package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesAction.LoadPropertiesAction
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesResult.LoadPropertiesResult
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

@BrowseScope
class PropertiesActionProcessor @Inject constructor(
        private val propertyRepository: PropertyRepository,
        private val schedulerProvider: BaseSchedulerProvider,
) {
    private val loadPropertiesProcessor =
        ObservableTransformer<LoadPropertiesAction, PropertiesResult> { actions ->
            actions.flatMap {
                propertyRepository.findAllProperties()
                    .filter { properties -> properties.isNotEmpty() }
                    .map { properties -> LoadPropertiesResult.Success(properties) }
                    .cast(LoadPropertiesResult::class.java)
                    .onErrorReturn(LoadPropertiesResult::Failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(LoadPropertiesResult.InFlight)
            }
        }

    private val updatePropertyProcessor = ObservableTransformer<PropertiesAction.UpdatePropertyAction, PropertiesResult> { actions ->
        actions.flatMap { action ->
            propertyRepository.updateProperty(action.property)
                .map { fullyUpdated -> PropertiesResult.UpdatePropertyResult.Updated(fullyUpdated) }
                .cast(PropertiesResult.UpdatePropertyResult::class.java)
                .onErrorReturn(PropertiesResult.UpdatePropertyResult::Failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(PropertiesResult.UpdatePropertyResult.InFlight)
        }
    }

    var actionProcessor = ObservableTransformer<PropertiesAction, PropertiesResult> { actions ->
        actions.publish { shared ->
            Observable.merge(
                shared.ofType(LoadPropertiesAction::class.java).compose(loadPropertiesProcessor),
                shared.ofType(PropertiesAction.UpdatePropertyAction::class.java).compose(updatePropertyProcessor)
            ).mergeWith(
                // Error for not implemented actions
                shared.filter { v ->
                    v !is PropertiesAction.LoadPropertiesAction
                            && v !is PropertiesAction.UpdatePropertyAction
                }.flatMap { w ->
                    Observable.error<PropertiesResult>(
                        IllegalArgumentException("Unknown Action type: $w"))
                }
            )
        }
    }
}