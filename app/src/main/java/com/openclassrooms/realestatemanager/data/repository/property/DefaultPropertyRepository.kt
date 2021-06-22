package com.openclassrooms.realestatemanager.data.repository.property

import com.openclassrooms.realestatemanager.data.cache.PropertyLocalDataSource
import com.openclassrooms.realestatemanager.data.remote.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject

@BrowseScope
class DefaultPropertyRepository
@Inject
constructor(
        val networkConnectionLiveData: NetworkConnectionLiveData,
        private val propertyRemoteDataSource: PropertyRemoteDataSource,
        private val propertyLocalDataSource: PropertyLocalDataSource
) : PropertyRepository {

    private var cachedProperties: MutableList<Property> =  mutableListOf()
    private var isInternetSubject: ReplaySubject<Boolean> = ReplaySubject.create()

    private var isInternetAvailable: Boolean? = null

    init {
        networkConnectionLiveData.observeForever { isInternetAvailable ->
            if(isInternetAvailable != this.isInternetAvailable) {
                this.isInternetAvailable = isInternetAvailable
                isInternetSubject.onNext(isInternetAvailable)
            }
        }
    }

    override fun findAllProperties(): Observable<List<Property>> {
        if(networkConnectionLiveData.value != null && cachedProperties.isNotEmpty()) {
            return  Observable.just(cachedProperties)
        }
        return isInternetSubject.compose(allProperties())
    }

    private fun allProperties() = ObservableTransformer<Boolean, List<Property>> { internetAvailable ->
        internetAvailable.flatMap { isInternetAvailable ->
            if (isInternetAvailable) {
                Single.zip(findRemoteProperties(), findLocalProperties(),
                        { remoteProperties, localProperties -> remoteProperties + localProperties })
                        .toObservable()
                        .flatMapIterable { properties -> properties }
                        .distinct(Property::id)
                        .toList()
                        .flatMap { properties ->
                            if(properties != cachedProperties) {
                                cachedProperties.clear()
                                cachedProperties.addAll(properties)
                                propertyLocalDataSource.saveProperties(properties)
                                        .andThen(Single.just(properties))
                            } else {
                                Single.just(cachedProperties)
                            }
                        }.toObservable()
            } else findLocalProperties().toObservable()
        }
    }

    private fun findRemoteProperties(): Single<List<Property>> {
        return propertyRemoteDataSource.findAllProperties()
    }

    private fun findLocalProperties(): Single<List<Property>> {
        if(cachedProperties.isEmpty()) {
            return propertyLocalDataSource.findAllProperties()
                    .doOnSuccess { localProperties -> cachedProperties.addAll(localProperties) }
        }
        if(networkConnectionLiveData.value!!) {
            return Single.just(cachedProperties)
        }
        return Single.just(emptyList())
    }
}