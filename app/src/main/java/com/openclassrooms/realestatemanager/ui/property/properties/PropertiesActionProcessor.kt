package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesAction.LoadPropertiesAction
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesResult.LoadPropertiesResult
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesResult.UpdatePropertyResult
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
                propertyRepository.findAllProperties()
                    .filter { properties -> properties.isNotEmpty() }
                    .map { properties -> LoadPropertiesResult.Success(false, properties) }
                    .cast(LoadPropertiesResult::class.java)
                    .onErrorReturn(LoadPropertiesResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(LoadPropertiesResult.InFlight)
            }
        }

    private val saveRemotelyLocalChangesProcessor =
        ObservableTransformer<LoadPropertiesAction, PropertiesResult> { actions ->
            actions.flatMap {
                propertyRepository.saveRemotelyLocalChanges(updates = true)
                    .map { updatedProperties -> UpdatePropertyResult.Updated(updatedProperties.isNotEmpty()) }
                    .cast(PropertiesResult::class.java)
                    .onErrorReturn(PropertiesResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
            }
                /*Observable.merge(
                    propertyRepository.saveRemotelyLocalChanges(updates = true).map {
                            updatedProperties -> UpdatePropertyResult.Updated(updatedProperties.isNotEmpty())
                    },
                    propertyRepository.saveRemotelyLocalChanges(creations = true).map {
                            createdProperties -> PropertiesResult.CreatePropertyResult.Created(createdProperties.isNotEmpty())
                    }
                ).cast(PropertiesResult::class.java)
                    .onErrorReturn(PropertiesResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(PropertiesResult.InFlight)
            }*/
        }

    var actionProcessor = ObservableTransformer<PropertiesAction, PropertiesResult> { actions ->
        actions.publish { shared ->
            shared.ofType(LoadPropertiesAction::class.java).compose {
                Observable.merge(
                    it.compose(loadPropertiesProcessor),
                    it.compose(saveRemotelyLocalChangesProcessor)
                )
            }.mergeWith(
                // Error for not implemented actions
                shared.filter { v -> v !is LoadPropertiesAction }.flatMap { w ->
                    Observable.error(
                        IllegalArgumentException("Unknown Action type: $w"))
                }
            )
        }
    }
}