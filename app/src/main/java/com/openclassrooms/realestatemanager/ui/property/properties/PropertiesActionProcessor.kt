package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository.CreateOrUpdate.CREATE
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository.CreateOrUpdate.UPDATE
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesAction.LoadPropertiesAction
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesResult.*
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
                    .onErrorReturn {
                        LoadPropertiesResult.Failure(it)
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(LoadPropertiesResult.InFlight)
            }
        }

    private val saveRemotelyLocalChangesProcessor =
        ObservableTransformer<LoadPropertiesAction, PropertiesResult> { actions ->
            actions.flatMap {
                propertyRepository.pushLocalChanges().flatMap { result ->
                    when(result.first) {
                        CREATE -> {
                            Observable.just(CreatePropertyResult.Created(result.second.isNotEmpty()))
                                .cast(CreatePropertyResult::class.java)
                                .onErrorReturn {
                                    CreatePropertyResult.Failure(it)
                                }
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui())
                        }
                        UPDATE -> {
                            Observable.just(UpdatePropertyResult.Updated(result.second.isNotEmpty()))
                                .cast(UpdatePropertyResult::class.java)
                                .onErrorReturn {
                                    UpdatePropertyResult.Failure(it)
                                }
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui())
                        }
                    }
                }
            }
        }

    var actionProcessor = ObservableTransformer<PropertiesAction, PropertiesResult> { actions ->
        actions.publish { shared ->
            shared.ofType(LoadPropertiesAction::class.java).compose {
                Observable.merge(
                    it.compose(saveRemotelyLocalChangesProcessor),
                    it.compose(loadPropertiesProcessor)
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