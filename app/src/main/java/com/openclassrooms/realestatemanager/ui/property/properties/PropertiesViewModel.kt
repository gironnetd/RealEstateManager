package com.openclassrooms.realestatemanager.ui.property.properties

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesResult.*
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_CREATED
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertiesViewModel
@Inject internal constructor(private val propertiesProcessor: PropertiesActionProcessor)
    : ViewModel(), MviViewModel<PropertiesIntent, PropertiesViewState> {

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
    }

    private fun actionFromIntent(intent: MviIntent): PropertiesAction {
        return when (intent) {
            is PropertiesIntent.InitialIntent -> PropertiesAction.LoadPropertiesAction
            is PropertiesIntent.LoadPropertiesIntent -> PropertiesAction.LoadPropertiesAction
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: $intent")
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
                        result.properties?.let { properties ->
                             if(properties.isEmpty()) {
                                 if(previousState.inProgress == true && previousState.properties == null) {
                                     previousState.copy(
                                         inProgress = true,
                                         properties = properties,
                                         error = null,
                                         uiNotification = null
                                     )
                                 } else if(previousState.inProgress == true && previousState.properties!!.isEmpty()){
                                     previousState.copy(
                                         inProgress = null,
                                         properties = null,
                                         error = null,
                                         uiNotification = null
                                     )
                                 } else {
                                     previousState
                                 }
                             } else {
                                 previousState.copy(
                                     inProgress = false,
                                     properties = properties,
                                     error = null,
                                     uiNotification = null
                                 )
                             }
                        } ?: previousState.copy(
                            inProgress = true,
                            properties = null,
                            error = null,
                            uiNotification = null
                        )
                    }
                    is LoadPropertiesResult.Failure -> {
                        previousState.copy(inProgress = false, error = result.error)
                    }
                    is LoadPropertiesResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            properties = null,
                            uiNotification = null
                        )
                    }
                }
                is CreatePropertyResult -> when(result) {
                    is CreatePropertyResult.Created -> {
                        previousState.copy(
                            inProgress = false,
                            properties = null,
                            error = null,
                            uiNotification = if (result.fullyCreated) { PROPERTIES_FULLY_CREATED } else null
                        )
                    }
                    is CreatePropertyResult.Failure -> {
                        previousState.copy(inProgress = false, error = result.error)
                    }
                    is CreatePropertyResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            properties = null,
                            error = null,
                            uiNotification = null
                        )
                    }
                }
                is UpdatePropertyResult -> when(result) {
                    is UpdatePropertyResult.Updated -> {
                        previousState.copy(
                            inProgress = false,
                            properties = null,
                            error = null,
                            uiNotification = if (result.fullyUpdated) { PROPERTIES_FULLY_UPDATED } else null
                        )
                    }
                    is UpdatePropertyResult.Failure -> {
                        previousState.copy(inProgress = false, error = result.error)
                    }
                    is UpdatePropertyResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            properties = null,
                        )
                    }
                }
            }
        }
    }
}

