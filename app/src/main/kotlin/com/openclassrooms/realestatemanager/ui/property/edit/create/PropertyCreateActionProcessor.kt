package com.openclassrooms.realestatemanager.ui.property.edit.create

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertyCreateActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

    private val createPropertyProcessor = ObservableTransformer<PropertyEditAction.CreatePropertyAction.CreateAction, PropertyEditResult.CreatePropertyResult> { actions ->
        actions.flatMap { action ->
            propertyRepository.createProperty(action.property)
                .map { fullyCreated -> PropertyEditResult.CreatePropertyResult.Created(fullyCreated) }
                .cast(PropertyEditResult.CreatePropertyResult::class.java)
                .onErrorReturn(PropertyEditResult.CreatePropertyResult::Failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(PropertyEditResult.CreatePropertyResult.InFlight)
        }
    }

    var actionProcessor = ObservableTransformer<PropertyEditAction, PropertyEditResult.CreatePropertyResult> { actions ->
        actions.publish { shared ->
            shared.ofType(PropertyEditAction.CreatePropertyAction.CreateAction::class.java).compose(createPropertyProcessor)
                .cast(PropertyEditResult.CreatePropertyResult::class.java)
                .mergeWith(
                    // Error for not implemented actions
                    shared.filter { v -> v !is PropertyEditAction.CreatePropertyAction }.flatMap { w ->
                        Observable.error(IllegalArgumentException("Unknown Action type: $w"))
                    }
                )
        }
    }

}