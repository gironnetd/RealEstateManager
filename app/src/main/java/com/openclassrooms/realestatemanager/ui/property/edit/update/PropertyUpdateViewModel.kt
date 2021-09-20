package com.openclassrooms.realestatemanager.ui.property.edit.update

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult.UpdatePropertyResult
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_UPDATED
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PropertyUpdateViewModel
@Inject internal constructor(private val propertyUpdateActionProcessor: PropertyUpdateActionProcessor)
    : ViewModel(), MviViewModel<PropertyUpdateIntent, PropertyEditViewState> {

    private var intentsSubject: PublishSubject<PropertyUpdateIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertyEditViewState> = compose()
    private val disposables = CompositeDisposable()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<PropertyUpdateIntent, PropertyUpdateIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                shared.filter { intent -> intent !is PropertyUpdateIntent.InitialIntent }
            }
        }

    override fun processIntents(intents: Observable<PropertyUpdateIntent>) {
        disposables.add(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<PropertyEditViewState> = statesSubject

    private fun compose(): Observable<PropertyEditViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(propertyUpdateActionProcessor.actionProcessor)
            .scan(PropertyEditViewState.idle(), reducer)
    }

    private fun actionFromIntent(intent: MviIntent): PropertyEditAction {
        return when (intent) {
            is PropertyUpdateIntent.UpdatePropertyIntent -> PropertyEditAction.UpdatePropertyAction.UpdatePropertyAction(intent.property)
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: " + intent)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction { previousState: PropertyEditViewState, result: UpdatePropertyResult ->
            when (result) {
                    is UpdatePropertyResult.Updated -> {
                        if(result.fullyUpdated) {
                            previousState.copy(
                                inProgress = false,
                                isSaved = true,
                                uiNotification = PROPERTIES_FULLY_UPDATED
                            )
                        } else {
                            previousState.copy(
                                inProgress = false,
                                isSaved = true,
                                uiNotification = PROPERTY_LOCALLY_UPDATED
                            )
                        }
                    }
                    is UpdatePropertyResult.Failure -> {
                        previousState.copy(inProgress = false, isSaved = false, error = result.error)
                    }
                    is UpdatePropertyResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            isSaved = false,
                            uiNotification = null,
                        )
                    }
                }
        }
    }
}