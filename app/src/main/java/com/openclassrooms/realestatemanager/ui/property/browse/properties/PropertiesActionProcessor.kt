package com.openclassrooms.realestatemanager.ui.property.browse.properties

import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

@BrowseScope
class PropertiesActionProcessor @Inject constructor(
        private val propertyRepository: PropertyRepository,
        private val schedulerProvider: BaseSchedulerProvider,
) {

    private val loadPropertiesProcessor: ObservableTransformer<
            PropertiesAction.LoadProperties, PropertiesResult> = ObservableTransformer {
        it.switchMap {
            propertyRepository.allProperties()
                    .toObservable()
                    .map {
                        PropertiesResult.LoadPropertiesTask.success(it)
                    }
                    .onErrorReturn {
                        PropertiesResult.LoadPropertiesTask.failure()
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(PropertiesResult.LoadPropertiesTask.inFlight())
        }
    }

    var actionProcessor: ObservableTransformer<PropertiesAction, PropertiesResult>

    init {
        this.actionProcessor = ObservableTransformer {
            it.publish { action ->
                action.ofType(PropertiesAction.LoadProperties::class.java)
                        .compose(loadPropertiesProcessor)
                        .mergeWith(action.filter { it !is PropertiesAction.LoadProperties }
                                .flatMap {
                                    Observable.error<PropertiesResult>(
                                            IllegalArgumentException("Unknown Action type"))
                                })
            }
        }
    }
}