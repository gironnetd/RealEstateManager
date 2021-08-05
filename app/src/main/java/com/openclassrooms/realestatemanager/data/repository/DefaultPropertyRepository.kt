package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import org.apache.commons.lang3.tuple.MutablePair
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPropertyRepository
@Inject constructor(
    val networkConnectionLiveData: NetworkConnectionLiveData,
    var remoteDataSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>,
    var cacheDataSource: DataSource<PropertyCacheSource, PhotoCacheSource>) : PropertyRepository {

    private var cachedProperties: MutablePair<Boolean?, MutableList<Property>> = MutablePair(null, mutableListOf())
    private var isInternetSubject: ReplaySubject<Boolean> = ReplaySubject.create()

    private var isInternetAvailable: Boolean? = null

    init {
        networkConnectionLiveData.observeForever { isInternetAvailable ->
            if (isInternetAvailable != this.isInternetAvailable) {
                this.isInternetAvailable = isInternetAvailable
                isInternetSubject.onNext(isInternetAvailable)
            }
        }
    }

    override fun findAllProperties(): Observable<MutablePair<Boolean?, MutableList<Property>?>> {
        if (networkConnectionLiveData.value != null && cachedProperties.right.isNotEmpty()) {
            return Observable.just(MutablePair(cachedProperties.left, cachedProperties.right))
        }
        return isInternetSubject.compose(allProperties()).startWith(findLocalProperties().toObservable())
    }

    private fun allProperties() =
        ObservableTransformer<Boolean, MutablePair<Boolean?, MutableList<Property>?>> { internetAvailable ->
            internetAvailable.flatMap { isInternetAvailable ->
                if (isInternetAvailable) {
                    findRemoteProperties().toObservable().flatMap { remoteProperties ->
                        if (cachedProperties.right.isEmpty()) {
                            cachedProperties.right.addAll(remoteProperties)
                            cacheDataSource.save(Property::class, remoteProperties)
                                .andThen(Observable.just(MutablePair(cachedProperties.left, cachedProperties.right)))
                        } else {
                            remoteProperties.forEachIndexed { propertiesIndex, remoteProperty ->

                                if (remoteProperty != cachedProperties.right[propertiesIndex] && remoteProperty.photos == cachedProperties.right[propertiesIndex].photos) {
                                    cachedProperties.right[propertiesIndex] = remoteProperty
                                    cacheDataSource.update(Property::class, remoteProperty)
                                }

                                if (remoteProperty != cachedProperties.right[propertiesIndex] && remoteProperty.photos != cachedProperties.right[propertiesIndex].photos) {
                                    if (remoteProperty.photos.size == cachedProperties.right[propertiesIndex].photos.size) {
                                        remoteProperty.photos.forEachIndexed { photoIndex, remotePhoto ->
                                            if (remotePhoto != cachedProperties.right[propertiesIndex].photos[photoIndex]) {
                                                cacheDataSource.save(Photo::class, remotePhoto)
                                            }
                                        }
                                    }

                                    if (remoteProperty.photos.size != cachedProperties.right[propertiesIndex].photos.size) {
                                        if (remoteProperty.photos.size < cachedProperties.right[propertiesIndex].photos.size) {
                                            if (!cachedProperties.right[propertiesIndex].photos.containsAll(remoteProperty.photos)) {
                                                remoteProperty.photos.forEach { remotePhoto ->
                                                    if (!cachedProperties.right[propertiesIndex].photos.contains(remotePhoto)) {
                                                        cachedProperties.right[propertiesIndex].photos.add(remotePhoto)
                                                        cacheDataSource.save(Photo::class, remotePhoto)
                                                    }
                                                }
                                            }
                                        }

                                        if (remoteProperty.photos.size > cachedProperties.right[propertiesIndex].photos.size) {
                                            remoteProperty.photos.forEach { remotePhoto ->
                                                if (!cachedProperties.right[propertiesIndex].photos.contains(remotePhoto)) {
                                                    cachedProperties.right[propertiesIndex].photos.add(remotePhoto)
                                                    cacheDataSource.save(Photo::class, remotePhoto)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Observable.just(MutablePair(false, cachedProperties.right))
                        }
                    }
                } else Observable.just(MutablePair(null, null))
            }
        }

    override fun findProperty(propertyId: String): Observable<Property> {
        return findCompleteProperty(propertyId).startWith(cacheDataSource.findById(Property::class,
            propertyId).toObservable())
    }

    private fun findCompleteProperty(propertyId: String): Observable<Property> {
        return if (networkConnectionLiveData.value == true) {
            Observable.zip(remoteDataSource.findById(Property::class, propertyId).toObservable(),
                cacheDataSource.findById(Property::class, propertyId).toObservable(),
                { remoteProperty, localProperty ->
                    {
                        if (remoteProperty != localProperty || remoteProperty.photos != localProperty.photos) {
                            cachedProperties.right[cachedProperties.right.indexOf(localProperty)] = remoteProperty
                            cacheDataSource.save(Property::class, remoteProperty)
                                .startWith(Observable.just(remoteProperty))
                        } else {
                            Observable.just(localProperty)
                        }
                    }
                }).flatMap { it.invoke() }
        } else cacheDataSource.findById(Property::class, propertyId).toObservable()
    }

    override fun updateProperty(propertyToUpdate: Property): Observable<Boolean> {
        val updatedPhotos = propertyToUpdate.photos.filter { photo -> photo.updated }

        val propertiesToUpdate: MutableList<Property> = mutableListOf()
        val photosToUpdate: MutableList<Photo> = mutableListOf()

        if (networkConnectionLiveData.value == true) {
            propertyToUpdate.updated = false
            updatedPhotos.forEach { photo -> photo.updated = false }

            cachedProperties.right.find { property -> property.id == propertyToUpdate.id }?.let {
                cachedProperties.right.remove(it)
                cachedProperties.right.add(propertyToUpdate)
            } ?: cachedProperties.right.add(propertyToUpdate)

            return cacheDataSource.findAllUpdated(Property::class).flatMap { allUpdatedProperties ->

                propertiesToUpdate.addAll(allUpdatedProperties)
                propertiesToUpdate.forEach { property -> property.updated = false }
                cacheDataSource.findAllUpdated(Photo::class)
            }.flatMap { allUpdatedPhotos ->

                photosToUpdate.addAll(allUpdatedPhotos)
                photosToUpdate.forEach { photo -> photo.updated = false }
                Single.just(true)
            }.toObservable().flatMap {

                if (propertiesToUpdate.isNotEmpty()) {

                    if (!propertiesToUpdate.contains(propertyToUpdate)) {
                        propertiesToUpdate.singleOrNull { property -> property.id == propertyToUpdate.id }
                            ?.let {
                                propertiesToUpdate.remove(it)
                                propertiesToUpdate.add(propertyToUpdate)
                            }
                    }

                    if (photosToUpdate.isNotEmpty()) {
                        remoteDataSource.update(Property::class, propertiesToUpdate)
                            .andThen(remoteDataSource.update(Photo::class, photosToUpdate))
                            .andThen(cacheDataSource.update(Property::class, propertiesToUpdate))
                            .andThen(cacheDataSource.update(Photo::class, photosToUpdate))
                            .andThen(Observable.just(true))
                    } else {
                        remoteDataSource.update(Property::class, propertiesToUpdate)
                            .andThen(cacheDataSource.update(Property::class, propertiesToUpdate))
                            .andThen(Observable.just(true))
                    }
                } else if (propertiesToUpdate.isEmpty() && photosToUpdate.isNotEmpty()) {

                    remoteDataSource.update(Photo::class, photosToUpdate)
                        .andThen(cacheDataSource.update(Photo::class, photosToUpdate))
                        .andThen(Observable.just(true))
                } else if (updatedPhotos.isNotEmpty()) {

                    remoteDataSource.update(Property::class, propertyToUpdate)
                        .andThen(remoteDataSource.update(Photo::class, updatedPhotos))
                        .andThen(cacheDataSource.update(Property::class, propertyToUpdate))
                        .andThen(cacheDataSource.update(Photo::class, updatedPhotos))
                        .andThen(Observable.just(true))
                } else {
                    remoteDataSource.update(Property::class, propertyToUpdate)
                        .andThen(cacheDataSource.update(Property::class, propertyToUpdate))
                        .andThen(Observable.just(true))
                }
            }
        } else {
            if (!propertyToUpdate.updated) { propertyToUpdate.updated = true }

            cachedProperties.right.find { property -> property.id == propertyToUpdate.id }?.let {
                cachedProperties.right.remove(it)
                cachedProperties.right.add(propertyToUpdate)
            } ?: cachedProperties.right.add(propertyToUpdate)

            return if (updatedPhotos.isNotEmpty()) {
                cacheDataSource.update(Property::class, propertyToUpdate)
                    .andThen(cacheDataSource.update(Photo::class, updatedPhotos))
                    .andThen(Observable.just(false))
            } else {
                cacheDataSource.update(Property::class, propertyToUpdate)
                    .andThen(Observable.just(false))
            }
        }
    }



    override fun updatePropertiesFromCache(): Observable<MutablePair<Boolean?, MutableList<Property>?>> {
        return isInternetSubject.compose(updateProperties())
    }

    private fun updateProperties() =
        ObservableTransformer<Boolean, MutablePair<Boolean?, MutableList<Property>?>> { internetAvailable ->
            internetAvailable.flatMap { isInternetAvailable ->
                if (isInternetAvailable) {
                    val propertiesToUpdate: MutableList<Property> = mutableListOf()
                    val photosToUpdate: MutableList<Photo> = mutableListOf()

                    cachedProperties.left = false

                    cacheDataSource.findAllUpdated(Property::class)
                        .flatMap { allUpdatedProperties ->

                            propertiesToUpdate.addAll(allUpdatedProperties)
                            propertiesToUpdate.forEach { property -> property.updated = false }
                            cacheDataSource.findAllUpdated(Photo::class)
                        }.flatMap { allUpdatedPhotos ->

                            photosToUpdate.addAll(allUpdatedPhotos)
                            photosToUpdate.forEach { photo -> photo.updated = false }
                            Single.just(true)
                        }.flatMapObservable {

                            if (propertiesToUpdate.isNotEmpty()) {
                                cachedProperties.left = true

                                if (photosToUpdate.isNotEmpty()) {
                                    remoteDataSource.update(Property::class, propertiesToUpdate)
                                        .andThen(remoteDataSource.update(Photo::class,
                                            photosToUpdate))
                                        .andThen(cacheDataSource.update(Property::class,
                                            propertiesToUpdate))
                                        .andThen(cacheDataSource.update(Photo::class, photosToUpdate))
                                        .andThen(Observable.just(MutablePair(true, null)))
                                } else {
                                    remoteDataSource.update(Property::class, propertiesToUpdate)
                                        .andThen(cacheDataSource.update(Property::class, propertiesToUpdate))
                                        .andThen(Observable.just(MutablePair(true, null)))
                                }
                            } else if (propertiesToUpdate.isEmpty() && photosToUpdate.isNotEmpty()) {
                                cachedProperties.left = true
                                remoteDataSource.update(Photo::class, photosToUpdate)
                                    .andThen(cacheDataSource.update(Photo::class, photosToUpdate))
                                    .andThen(Observable.just(MutablePair(true, null)))
                            } else {
                                Observable.just(MutablePair(null, null))
                            }
                        }
                } else Observable.just(MutablePair(null, null))
            }
        }

    private fun findRemoteProperties(): Single<List<Property>> {
        return remoteDataSource.findAll(Property::class)
    }

    private fun findLocalProperties(): Single<MutablePair<Boolean?, MutableList<Property>?>> {
        if (cachedProperties.right.isEmpty()) {
            return cacheDataSource.findAll(Property::class)//.toObservable()
                .doOnSuccess { localProperties -> cachedProperties.right.addAll(localProperties) }.flatMap {
                    Single.just(MutablePair(cachedProperties.left, cachedProperties.right))
                }
        }

        return Single.just(MutablePair(null, null))
    }
}

