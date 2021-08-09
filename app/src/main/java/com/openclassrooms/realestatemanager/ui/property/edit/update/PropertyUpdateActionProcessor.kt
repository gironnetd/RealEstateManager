package com.openclassrooms.realestatemanager.ui.property.edit.update

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction.PopulatePropertyAction.PopulatePropertyAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction.UpdatePropertyAction.UpdatePropertyAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult.UpdatePropertyResult

import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertyUpdateActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

    private val populatePropertyProcessor =
        ObservableTransformer<PopulatePropertyAction, PropertyEditResult.PopulatePropertyResult> { actions ->
            actions.flatMap { action ->
                propertyRepository.findProperty(action.propertyId)
                    .map { property -> PropertyEditResult.PopulatePropertyResult.Success(property) }
                    .cast(PropertyEditResult.PopulatePropertyResult::class.java)
                    .onErrorReturn(PropertyEditResult.PopulatePropertyResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(PropertyEditResult.PopulatePropertyResult.InFlight)
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

    var actionProcessor = ObservableTransformer<PropertyEditAction, PropertyEditResult.UpdatePropertyResult> { actions ->
        actions.publish { shared ->
//            Observable.merge(
//                shared.ofType(PopulatePropertyAction::class.java).compose(populatePropertyProcessor),
//                shared.ofType(UpdatePropertyAction::class.java).compose(updatePropertyProcessor)
//            )
            shared.ofType(UpdatePropertyAction::class.java).compose(updatePropertyProcessor)
                .cast(PropertyEditResult.UpdatePropertyResult::class.java)
                .mergeWith(
                    // Error for not implemented actions
                    shared.filter { v ->
                        v !is PropertyEditAction.UpdatePropertyAction
                    }.flatMap { w ->
                        Observable.error(IllegalArgumentException("Unknown Action type: $w"))
                    }
                )
        }
    }
}