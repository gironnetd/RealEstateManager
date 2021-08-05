package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailAction.PopulatePropertyAction
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailResult.PopulatePropertyResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyDetailActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

    private val populatePropertyProcessor =
        ObservableTransformer<PopulatePropertyAction, PopulatePropertyResult> { actions ->
            actions.flatMap { action ->
                propertyRepository.findProperty(action.propertyId)
                    .map { property -> PopulatePropertyResult.Success(property) }
                    .cast(PopulatePropertyResult::class.java)
                    .onErrorReturn(PopulatePropertyResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(PopulatePropertyResult.InFlight)
            }
        }

    var actionProcessor = ObservableTransformer<PropertyDetailAction, PropertyDetailResult> { actions ->
        actions.publish { shared ->
                shared.ofType(PopulatePropertyAction::class.java).compose(populatePropertyProcessor)
                    .cast(PropertyDetailResult::class.java)
                    .mergeWith(
                        shared.filter { v ->
                            v !is PopulatePropertyAction
                        }.flatMap { w ->
                            Observable.error(IllegalArgumentException("Unknown Action type: $w"))
                        }
                    )
        }
    }
}