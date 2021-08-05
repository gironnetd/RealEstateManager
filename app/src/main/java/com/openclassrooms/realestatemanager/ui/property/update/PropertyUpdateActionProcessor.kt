package com.openclassrooms.realestatemanager.ui.property.update

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateAction.PopulatePropertyAction
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateAction.UpdatePropertyAction
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateResult.PopulatePropertyResult
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateResult.UpdatePropertyResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertyUpdateActionProcessor
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

    private val updatePropertyProcessor = ObservableTransformer<UpdatePropertyAction, UpdatePropertyResult> { actions ->
        actions.flatMap { action ->
            propertyRepository.updateProperty(action.property)
                .map { fullyUpdated -> UpdatePropertyResult.Updated(fullyUpdated) }
                .cast(UpdatePropertyResult::class.java)
                .onErrorReturn(UpdatePropertyResult::Failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(UpdatePropertyResult.InFlight)
        }
    }

    var actionProcessor = ObservableTransformer<PropertyUpdateAction, PropertyUpdateResult> { actions ->
        actions.publish { shared ->
            Observable.merge(
                shared.ofType(PopulatePropertyAction::class.java).compose(populatePropertyProcessor),
                shared.ofType(UpdatePropertyAction::class.java).compose(updatePropertyProcessor)
            )
                .mergeWith(
                    // Error for not implemented actions
                    shared.filter { v ->
                        v !is PopulatePropertyAction && v !is UpdatePropertyAction
                    }.flatMap { w ->
                        Observable.error(
                            IllegalArgumentException("Unknown Action type: $w"))
                    }
                )
        }
    }
}