package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository.CreateOrUpdate.CREATE
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository.CreateOrUpdate.UPDATE
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import io.reactivex.Completable
import io.reactivex.Completable.complete
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPropertyRepository
@Inject constructor(
    val networkConnectionLiveData: NetworkConnectionLiveData,
    var remoteDataSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>,
    var cacheDataSource: DataSource<PropertyCacheSource, PhotoCacheSource>) : PropertyRepository {

    var cachedProperties: MutableList<Property> = mutableListOf()
    private var isInternetSubject: ReplaySubject<Boolean> = ReplaySubject.create()

    init {
        networkConnectionLiveData.observeForever { isInternetAvailable ->
            isInternetSubject.onNext(isInternetAvailable)
        }
    }

    override fun findAllProperties(): Observable<List<Property>> {
        return isInternetSubject
            .flatMap { isInternetAvailable ->
                if (isInternetAvailable) {
                    Observable
                        .interval(1, TimeUnit.SECONDS)
                        .filter { cachedProperties.all { property -> !property.locallyUpdated }
                                && cachedProperties.all { property -> !property.locallyCreated } }
                        .map { it }
                        .take(1)
                        .flatMap { allProperties() }
                } else {
                    Observable.just(emptyList())
                }
            }
            .startWith(findLocalProperties())
    }

    private fun allProperties(): Observable<List<Property>> {
        return findRemoteProperties().toObservable().flatMap { remoteProperties ->
            if (cachedProperties.isEmpty()) {
                cachedProperties.addAll(remoteProperties)
                cacheDataSource.save(Property::class, remoteProperties).andThen(Observable.just(remoteProperties))
            } else {
                if(remoteProperties.sortedBy { it.id }.zip(cachedProperties.sortedBy { it.id })
                        .all { (remoteProperty, localProperty) -> remoteProperty == localProperty }) {
                    Observable.empty()
                } else {
                    Observable.fromIterable(remoteProperties).flatMapCompletable { remoteProperty ->
                        val cachedProperty = cachedProperties.find { property -> property.id == remoteProperty.id  }
                        if(remoteProperty != cachedProperty) {
                            cachedProperties[cachedProperties.indexOf(cachedProperty)].update(remoteProperty)
                            cacheDataSource.update(Property::class, remoteProperty)
                        } else { complete() }
                    }.andThen(Observable.just(remoteProperties))
                }
            }
        }
    }

    override fun findProperty(propertyId: String): Observable<Property> {
        return findCompleteProperty(propertyId).startWith(cacheDataSource.findById(
            Property::class,
            propertyId).toObservable())
    }

    private fun findCompleteProperty(propertyId: String): Observable<Property> {
        return if (networkConnectionLiveData.value == true) {
            Observable.zip(remoteDataSource.findById(Property::class, propertyId).toObservable(),
                cacheDataSource.findById(Property::class, propertyId).toObservable(),
                { remoteProperty, localProperty ->
                    {
                        if (remoteProperty != localProperty || remoteProperty.photos != localProperty.photos) {
                            cachedProperties[cachedProperties.indexOf(localProperty)] = remoteProperty
                            cacheDataSource.save(Property::class, remoteProperty)
                                .startWith(Observable.just(remoteProperty))
                        } else {
                            Observable.just(localProperty)
                        }
                    }
                }).flatMap { it.invoke() }
        } else cacheDataSource.findById(Property::class, propertyId).toObservable()
    }

    override fun updateProperty(updatedProperty: Property): Observable<Boolean> {
        return Observable.just(networkConnectionLiveData.value).flatMapCompletable { isInternetAvailable ->
            if(isInternetAvailable) { updateRemotelyProperty(updatedProperty = updatedProperty) }
            else { updateLocallyProperty(updatedProperty = updatedProperty) }
        }.andThen( ObservableSource { observer ->
            cachedProperties.single { property -> property.id == updatedProperty.id }
                .let { property -> cachedProperties[cachedProperties.indexOf(property)] = updatedProperty
                }
            observer.onNext(!updatedProperty.locallyUpdated)
        })
    }

    private fun updateRemotelyProperty(updatedProperty: Property): Completable {
        return remoteDataSource.update(Property::class, updatedProperty)
            .andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyUpdated }) {
                    remoteDataSource.update(
                        Photo::class, updatedProperty.photos
                        .filter { photo -> photo.locallyUpdated })
                } else { complete() }
            ).andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyCreated }) {
                    remoteDataSource.save(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyCreated } )
                } else { complete() }
            ).andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyDeleted }) {
                    remoteDataSource.delete(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyDeleted } )
                } else { complete() }
            )
            .andThen(cacheDataSource.update(Property::class, updatedProperty.apply { locallyUpdated = false }))
            .andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyUpdated }) {
                    cacheDataSource.update(
                        Photo::class, updatedProperty.photos
                        .filter { photo -> photo.locallyUpdated }
                        .onEach { photo -> photo.locallyUpdated = false })
                } else { complete() }
            ).andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyCreated }) {
                    cacheDataSource.save(
                        Photo::class, updatedProperty.photos
                        .filter { photo -> photo.locallyCreated }
                        .onEach { photo -> photo.locallyCreated = false })
                } else { complete() }
            ).andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyDeleted }) {
                    cacheDataSource.delete(
                        Photo::class, updatedProperty.photos
                        .filter { photo -> photo.locallyDeleted }
                        .onEach { photo -> photo.locallyDeleted = false }
                        .also { updatedProperty.photos.removeAll(it) }
                    )
                } else { complete() }
            )
    }

    private fun updateLocallyProperty(updatedProperty: Property): Completable {
        return cacheDataSource.update(Property::class, updatedProperty.apply { locallyUpdated = true })
            .andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyUpdated }) {
                    cacheDataSource.update(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyUpdated })
                } else { complete() }
            ).andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyCreated }) {
                    cacheDataSource.save(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyCreated })
                } else { complete() })
            .andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyDeleted && !photo.locallyCreated }) {
                    cacheDataSource.update(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyDeleted })
                } else { complete() })
            .andThen(
                if(updatedProperty.photos.any { photo -> photo.locallyDeleted && photo.locallyCreated }) {
                    cacheDataSource.delete(Photo::class, updatedProperty.photos.filter { photo -> photo.locallyDeleted }
                        .also { updatedProperty.photos.removeAll(it) }
                    )
                } else { complete() }
            )
    }

    override fun createProperty(createdProperty: Property): Observable<Boolean> {
        return Observable.just(networkConnectionLiveData.value).flatMapCompletable { isInternetAvailable ->
            if(isInternetAvailable) { createRemotelyProperty(createdProperty = createdProperty) }
            else { createLocallyProperty(createdProperty = createdProperty) }
        }.andThen( ObservableSource { observer ->
            cachedProperties.add(createdProperty)
            observer.onNext(!createdProperty.locallyCreated)
        })
    }

    private fun createRemotelyProperty(createdProperty: Property): Completable {
        return remoteDataSource.save(Property::class, createdProperty)
            .andThen(
                if(createdProperty.photos.isNotEmpty()) {
                    remoteDataSource.save(Photo::class, createdProperty.photos)
                } else { complete() }
            )
            .andThen(cacheDataSource.save(Property::class, createdProperty.apply { locallyCreated = false }))
            .andThen(
                if(createdProperty.photos.isNotEmpty()) {
                    cacheDataSource.save(Photo::class, createdProperty.photos.onEach { photo -> photo.locallyCreated = false
                        if(photo.locallyUpdated) { photo.locallyUpdated = false }
                    })
                } else { complete() }
            )
    }

    private fun createLocallyProperty(createdProperty: Property): Completable {
        return cacheDataSource.save(Property::class, createdProperty.apply { locallyCreated = true })
            .andThen(
                if(createdProperty.photos.isNotEmpty()) {
                    cacheDataSource.save(Photo::class, createdProperty.photos.onEach { photo -> photo.locallyCreated = true
                        if(photo.locallyUpdated) { photo.locallyUpdated = false }
                    })
                } else { complete() }
            )
    }

    enum class CreateOrUpdate { UPDATE, CREATE }

    override fun pushLocalChanges(): Observable<Pair<CreateOrUpdate, List<Property>>> {
        return isInternetSubject.filter { isInternetAvailable -> isInternetAvailable }.flatMap {
            Observable.merge(
                createProperties(),
                updateProperties()
            )
        }
    }

    fun updateProperties(): Observable<Pair<CreateOrUpdate, List<Property>>> {
        return Observable.just(
            cachedProperties.filter { property -> property.locallyUpdated && !property.locallyCreated }).flatMap { properties ->
            if(properties.isNotEmpty()) {
                remoteDataSource.update(Property::class, properties)
                    .andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyUpdated && !property.locallyCreated  } }) {
                            val updatedPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyUpdated && !property.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyUpdated && !property.locallyCreated } }
                                .flatten()
                            remoteDataSource.update(Photo::class, updatedPhotos)
                        } else { complete() }
                    ).andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyCreated } }) {
                            val createdPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyCreated } }
                                .flatten()
                            remoteDataSource.save(Photo::class, createdPhotos)
                        } else { complete() }
                    ).andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyDeleted } }) {
                            val deletedPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyDeleted }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyDeleted } }
                                .flatten()
                            remoteDataSource.delete(Photo::class, deletedPhotos)
                        } else { complete() }
                    )
                    .andThen(cacheDataSource.update(Property::class, properties.onEach { property -> property.locallyUpdated = false }))
                    .andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyUpdated && !property.locallyCreated } }) {
                            val updatedPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyUpdated && !property.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyUpdated && !property.locallyCreated } }
                                .flatten()
                            cacheDataSource.update(Photo::class, updatedPhotos.onEach { photo -> photo.locallyUpdated = false })
                        } else { complete() }
                    ).andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyCreated } }) {
                            val createdPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyCreated } }
                                .flatten()
                            cacheDataSource.update(Photo::class, createdPhotos.onEach { photo -> photo.locallyCreated = false
                                if(photo.locallyUpdated) { photo.locallyUpdated = false }
                            })
                        } else { complete() }
                    ).andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyDeleted } }) {
                            val deletedPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyDeleted }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyDeleted } }
                                .flatten()
                            cacheDataSource.delete(Photo::class, deletedPhotos)
                        } else { complete() }
                    )
                    .andThen( ObservableSource { observer -> observer.onNext(Pair(UPDATE, properties)) })
            } else { Observable.empty() }
        }
    }

    private fun createProperties(): Observable<Pair<CreateOrUpdate, List<Property>>> {
        return Observable.just(
            cachedProperties.filter { property -> property.locallyCreated }).flatMap { properties ->
            if(properties.isNotEmpty()) {
                remoteDataSource.save(Property::class, properties)
                    .andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyCreated } }) {
                            val createdPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyCreated } }
                                .flatten()
                            remoteDataSource.save(Photo::class, createdPhotos)
                        } else { complete() }
                    )
                    .andThen(cacheDataSource.update(Property::class, properties.onEach { property ->
                        property.locallyCreated = false
                    }))
                    .andThen(
                        if(properties.any { property -> property.photos.any { photo -> photo.locallyCreated } }) {
                            val createdPhotos = properties
                                .filter{ property -> property.photos.any { photo -> photo.locallyCreated }  }
                                .map { property -> property.photos.filter { photo -> photo.locallyCreated } }
                                .flatten()
                            cacheDataSource.update(Photo::class, createdPhotos.onEach { photo -> photo.locallyCreated = false
                                if(photo.locallyUpdated) { photo.locallyUpdated = false }
                            })
                        } else { complete() }
                    )
                    .andThen( ObservableSource { observer -> observer.onNext(Pair(CREATE, properties)) })
            } else { Observable.empty() }
        }
    }

    private fun findRemoteProperties(): Single<List<Property>> {
        return remoteDataSource.findAll(Property::class)
    }

    private fun findLocalProperties(): Observable<List<Property>> {
        return if (cachedProperties.isEmpty()) {
            cacheDataSource.findAll(Property::class)
                .doOnSuccess { localProperties -> cachedProperties.addAll(localProperties) }.flatMapObservable {
                    Observable.just(cachedProperties)
                }
        } else { Observable.empty() }
    }
}

