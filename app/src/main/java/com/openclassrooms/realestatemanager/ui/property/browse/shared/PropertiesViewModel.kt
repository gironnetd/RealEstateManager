package com.openclassrooms.realestatemanager.ui.property.browse.shared

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.base.BaseIntent
import com.openclassrooms.realestatemanager.base.BaseViewModel
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesResult.LoadPropertiesResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertiesViewModel
@Inject internal constructor(private val propertiesProcessor: PropertiesActionProcessor)
    : ViewModel(), BaseViewModel<PropertiesIntent, PropertiesViewState> {

    private var intentsSubject: PublishSubject<PropertiesIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertiesViewState> = compose()
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

    override fun states(): Observable<PropertiesViewState> = statesSubject

    private fun compose(): Observable<PropertiesViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(propertiesProcessor.actionProcessor)
            .scan(PropertiesViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: BaseIntent): PropertiesAction {
        return when (intent) {
            is PropertiesIntent.InitialIntent -> PropertiesAction.LoadPropertiesAction
            is PropertiesIntent.LoadPropertiesIntent -> PropertiesAction.LoadPropertiesAction
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: " + intent)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction { previousState: PropertiesViewState, result: PropertiesResult ->
            when (result) {
                is LoadPropertiesResult -> when(result) {
                    is LoadPropertiesResult.Success -> {
                        previousState.copy(
                            inProgress = false,
                            properties = result.properties,
                            uiNotification = null,
                        )
                    }
                    is LoadPropertiesResult.Failure -> {
                        previousState.copy(inProgress = false, error = result.error)
                    }
                    is LoadPropertiesResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            properties = null,
                            uiNotification = null,
                        )
                    }
                }
                else -> { previousState }
            }
        }
    }
}

