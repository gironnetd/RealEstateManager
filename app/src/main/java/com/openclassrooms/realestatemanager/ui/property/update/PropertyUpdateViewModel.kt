package com.openclassrooms.realestatemanager.ui.property.update

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateResult.PopulatePropertyResult
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateResult.UpdatePropertyResult
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateViewState.UiNotification.PROPERTY_LOCALLY_UPDATED
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class PropertyUpdateViewModel
@Inject internal constructor(private val propertyUpdateActionProcessor: PropertyUpdateActionProcessor)
    : ViewModel(), MviViewModel<PropertyUpdateIntent, PropertyUpdateViewState> {

    private var intentsSubject: PublishSubject<PropertyUpdateIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertyUpdateViewState> = compose()
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

    override fun states(): Observable<PropertyUpdateViewState> = statesSubject

    private fun compose(): Observable<PropertyUpdateViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(propertyUpdateActionProcessor.actionProcessor)
            .scan(PropertyUpdateViewState.idle(), reducer)
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: MviIntent): PropertyUpdateAction {
        return when (intent) {
            is PropertyUpdateIntent.InitialIntent -> PropertyUpdateAction.PopulatePropertyAction(intent.propertyId)
            is PropertyUpdateIntent.UpdatePropertyIntent -> PropertyUpdateAction.UpdatePropertyAction(intent.property)
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: " + intent)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction { previousState: PropertyUpdateViewState, result: PropertyUpdateResult ->
            when (result) {
                is PopulatePropertyResult -> when(result) {
                    is PopulatePropertyResult.Success -> {
                        previousState.copy(
                            inProgress = false,
                            isSaved = false,
                            error = null,
                            uiNotification = null
                        )
                    }
                    is PopulatePropertyResult.Failure -> {
                        Timber.i(result.error)
                        previousState.copy(
                            inProgress = false,
                            isSaved = false,
                            error = result.error,
                            uiNotification = null
                        )
                    }
                    is PopulatePropertyResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            isSaved = false,
                            error = null,
                            uiNotification = null
                        )
                    }
                }
                is UpdatePropertyResult -> when(result) {
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
                        Timber.i(result.error)
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
}