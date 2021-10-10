package com.openclassrooms.realestatemanager.ui.property.edit.update

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction.UpdatePropertyAction.UpdateAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult.UpdatePropertyResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertyUpdateActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

    private val updatePropertyProcessor = ObservableTransformer<UpdateAction, UpdatePropertyResult> { actions ->
        actions.flatMap { action ->
            propertyRepository.updateProperty(action.property)
                .map { fullyUpdated -> UpdatePropertyResult.Updated(fullyUpdated) }
                .cast(UpdatePropertyResult::class.java)
                .onErrorReturn(UpdatePropertyResult::Failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
        }
    }

    var actionProcessor = ObservableTransformer<PropertyEditAction, UpdatePropertyResult> { actions ->
        actions.publish { shared ->
            shared.ofType(UpdateAction::class.java).compose(updatePropertyProcessor)
                .cast(UpdatePropertyResult::class.java)
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