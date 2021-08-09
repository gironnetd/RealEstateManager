package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesAction.LoadPropertiesAction
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesResult.LoadPropertiesResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class PropertiesActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

    private val loadPropertiesProcessor =
        ObservableTransformer<LoadPropertiesAction, PropertiesResult> { actions ->
            actions.flatMap {
                propertyRepository.updatePropertiesFromCache()
                    .mergeWith(propertyRepository.findAllProperties())
                    .filter { properties ->
                        return@filter properties.right?.isNotEmpty() == true || properties.left == true
                    }
                    .map { properties -> LoadPropertiesResult.Success(properties.left, properties.right) }
                    .cast(LoadPropertiesResult::class.java)
                    .onErrorReturn(LoadPropertiesResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(LoadPropertiesResult.InFlight)
            }
        }

    var actionProcessor = ObservableTransformer<PropertiesAction, PropertiesResult> { actions ->
        actions.publish { shared ->
            shared.ofType(LoadPropertiesAction::class.java).compose(loadPropertiesProcessor).mergeWith(
                // Error for not implemented actions
                shared.filter { v -> v !is LoadPropertiesAction }.flatMap { w ->
                    Observable.error(
                        IllegalArgumentException("Unknown Action type: $w"))
                }
            )
        }
    }
}