package com.openclassrooms.realestatemanager.ui.property.browse.properties

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.base.BaseIntent
import com.openclassrooms.realestatemanager.base.BaseViewModel
import com.openclassrooms.realestatemanager.base.model.TaskStatus
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertiesViewModel @Inject internal constructor(
        private val propertiesProcessor: PropertiesActionProcessor,
) : ViewModel(), BaseViewModel<PropertiesIntent, PropertiesUiModel> {

    private var intentsSubject: PublishSubject<PropertiesIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertiesUiModel> = compose()
    private val disposables = CompositeDisposable()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<PropertiesIntent, PropertiesIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                        shared.ofType(PropertiesIntent.InitialIntent::class.java).take(1),
                        shared.filter { intent -> intent !is PropertiesIntent.InitialIntent }
                )
            }
        }

    override fun processIntents(intents: Observable<PropertiesIntent>) {
        disposables.add(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<PropertiesUiModel> = statesSubject

    private fun compose(): Observable<PropertiesUiModel> {
        return intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(propertiesProcessor.actionProcessor)
                .scan(PropertiesUiModel.Idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0)
    }

    private fun actionFromIntent(intent: BaseIntent): PropertiesAction {
        return when (intent) {
            is PropertiesIntent.InitialIntent -> PropertiesAction.LoadProperties
            is PropertiesIntent.LoadPropertiesIntent -> PropertiesAction.LoadProperties
            else -> throw UnsupportedOperationException(
                    "Oops, that looks like an unknown intent: " + intent)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer: BiFunction<PropertiesUiModel, PropertiesResult, PropertiesUiModel> =
                BiFunction<PropertiesUiModel, PropertiesResult, PropertiesUiModel> { previousState, result ->
                    when (result) {
                        is PropertiesResult.LoadPropertiesTask -> {
                            when (result.status) {
                                TaskStatus.SUCCESS -> PropertiesUiModel.Success(
                                        result.properties)
                                TaskStatus.FAILURE -> PropertiesUiModel.Failed
                                TaskStatus.IN_FLIGHT -> PropertiesUiModel.InProgress
                                else -> PropertiesUiModel.Idle()
                            }
                        }
                    }
                }
    }
}

