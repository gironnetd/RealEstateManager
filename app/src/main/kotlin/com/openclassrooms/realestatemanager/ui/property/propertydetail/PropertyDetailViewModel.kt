package com.openclassrooms.realestatemanager.ui.property.propertydetail

import androidx.lifecycle.ViewModel
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewModel
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailResult.PopulatePropertyResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class PropertyDetailViewModel
@Inject internal constructor(private val propertyDetailProcessor: PropertyDetailActionProcessor)
    : ViewModel(), MviViewModel<PropertyDetailIntent, PropertyDetailViewState> {

    private var intentsSubject: PublishSubject<PropertyDetailIntent> = PublishSubject.create()
    private val statesSubject: Observable<PropertyDetailViewState> = compose()
    private val disposables = CompositeDisposable()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<PropertyDetailIntent, PropertyDetailIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                shared.filter { intent -> intent !is PropertyDetailIntent.InitialIntent }
            }
        }

    override fun processIntents(intents: Observable<PropertyDetailIntent>) {
        disposables.add(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<PropertyDetailViewState> = statesSubject

    private fun compose(): Observable<PropertyDetailViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(propertyDetailProcessor.actionProcessor)
            .scan(PropertyDetailViewState.idle(), reducer)
    }

    private fun actionFromIntent(intent: MviIntent): PropertyDetailAction {
        return when (intent) {
            is PropertyDetailIntent.PopulatePropertyIntent -> PropertyDetailAction.PopulatePropertyAction(intent.propertyId)
            else -> throw UnsupportedOperationException("Oops, that looks like an unknown intent: $intent")
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction { previousState: PropertyDetailViewState, result: PropertyDetailResult ->
            when (result) {
                is PopulatePropertyResult -> when(result) {
                    is PopulatePropertyResult.Success -> {
                        previousState.copy(
                            inProgress = false,
                            property = result.property,
                            error = null,
                        )
                    }
                    is PopulatePropertyResult.Failure -> {
                        Timber.i(result.error)
                        previousState.copy(
                            inProgress = false,
                            property = null,
                            error = result.error,
                        )
                    }
                    is PopulatePropertyResult.InFlight -> {
                        previousState.copy(
                            inProgress = true,
                            property = null,
                            error = null,
                        )
                    }
                }
            }
        }
    }
}