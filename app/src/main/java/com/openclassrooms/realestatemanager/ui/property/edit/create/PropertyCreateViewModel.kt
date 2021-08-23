package com.openclassrooms.realestatemanager.ui.property.edit.create

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyCreateIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertyCreateViewModel
@Inject internal constructor(private val propertyCreateActionProcessor: PropertyCreateActionProcessor)
    : ViewModel(), MviViewModel<PropertyCreateIntent, PropertyEditViewState> {

    private var intentsSubject: PublishSubject<PropertyCreateIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertyEditViewState> = compose()
    private val disposables = CompositeDisposable()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<PropertyCreateIntent, PropertyCreateIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                shared.filter { intent -> intent !is PropertyCreateIntent.InitialIntent }
            }
        }

    override fun processIntents(intents: Observable<PropertyCreateIntent>) {
        disposables.add(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<PropertyEditViewState> = statesSubject

    private fun compose(): Observable<PropertyEditViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(propertyCreateActionProcessor.actionProcessor)
            .scan(PropertyEditViewState.idle(), reducer)
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: MviIntent): PropertyEditAction {
        return when (intent) {
            is PropertyCreateIntent.CreatePropertyIntent -> PropertyEditAction.CreatePropertyAction.CreatePropertyAction(intent.property)
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: " + intent)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction { previousState: PropertyEditViewState, result: PropertyEditResult.CreatePropertyResult ->
            when (result) {
                is PropertyEditResult.CreatePropertyResult.Created -> {
                    if(result.fullyCreated) {
                        previousState.copy(
                            inProgress = false,
                            isSaved = true,
                            uiNotification = PropertyEditViewState.UiNotification.PROPERTIES_FULLY_CREATED
                        )
                    } else {
                        previousState.copy(
                            inProgress = false,
                            isSaved = true,
                            uiNotification = PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_CREATED
                        )
                    }
                }
                is PropertyEditResult.CreatePropertyResult.Failure -> {
                    previousState.copy(
                        inProgress = false,
                        isSaved = false,
                        error = result.error,
                        uiNotification = null
                    )
                }
                is PropertyEditResult.CreatePropertyResult.InFlight -> {
                    previousState.copy(
                        inProgress = true,
                        isSaved = false,
                        error = null,
                        uiNotification = null
                    )
                }
            }
        }
    }
}