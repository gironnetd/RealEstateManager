package com.openclassrooms.realestatemanager.ui.property.edit.create

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditAction.CreatePropertyAction.CreateAction
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditResult.CreatePropertyResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertyCreateActionProcessor
@Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {

    private val createPropertyProcessor = ObservableTransformer<CreateAction, CreatePropertyResult> { actions ->
        actions.flatMap { action ->
            propertyRepository.createProperty(action.property)
                .map { fullyCreated -> CreatePropertyResult.Created(fullyCreated) }
                .cast(CreatePropertyResult::class.java)
                .onErrorReturn(CreatePropertyResult::Failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(CreatePropertyResult.InFlight)
        }
    }

    var actionProcessor = ObservableTransformer<PropertyEditAction, CreatePropertyResult> { actions ->
        actions.publish { shared ->
            shared.ofType(CreateAction::class.java).compose(createPropertyProcessor)
                .cast(CreatePropertyResult::class.java)
                .mergeWith(
                    // Error for not implemented actions
                    shared.filter { v -> v !is PropertyEditAction.CreatePropertyAction }.flatMap { w ->
                        Observable.error(IllegalArgumentException("Unknown Action type: $w"))
                    }
                )
        }
    }
}
