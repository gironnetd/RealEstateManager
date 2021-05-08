package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
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
            ObservableTransformer<PropertiesAction.LoadProperties, PropertiesResult> { actions ->
        actions.flatMap {
            propertyRepository.findAllProperties()
                    .filter { properties -> properties.isNotEmpty() }
                    .map { properties -> PropertiesResult.LoadPropertiesTask.success(properties) }
                    .onErrorReturn { PropertiesResult.LoadPropertiesTask.failure() }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(PropertiesResult.LoadPropertiesTask.inFlight())
        }
    }

    var actionProcessor = ObservableTransformer<PropertiesAction, PropertiesResult> { actions ->
        actions.publish { action ->
            action.ofType(PropertiesAction.LoadProperties::class.java)
                    .compose(loadPropertiesProcessor)
                    .mergeWith(action.filter { it !is PropertiesAction.LoadProperties }
                            .flatMap {
                                Observable.error(IllegalArgumentException("Unknown Action type"))
                            })
        }
    }
}