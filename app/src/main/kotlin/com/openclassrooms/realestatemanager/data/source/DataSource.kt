package com.openclassrooms.realestatemanager.data.source

import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoSource
import com.openclassrooms.realestatemanager.data.source.property.PropertySource
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.Property
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class DataSource<T : PropertySource, U : PhotoSource>
constructor(var propertySource: T, var photoSource: U) {

    open fun <T : Any> count(type: KClass<T>): Single<Int> {
        return when (type) {
            Property::class -> { propertySource.count() }
            Photo::class -> { photoSource.count() }
            else -> Single.error(Throwable(ClassNotFoundException()))
        }
    }

    open fun <T : Any> save(type: KClass<T>, value: T): Completable {
        return when (type) {
            Property::class -> {
                propertySource.saveProperty(value as Property)
                    .andThen(photoSource.savePhotos((value as Property).photos))
            }
            Photo::class -> { photoSource.savePhoto(value as Photo) }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> save(type: KClass<T>, value: List<T>): Completable {
        return when (type) {
            Property::class -> {
                value.filterIsInstance(Property::class.java).let { properties ->
                    Observable.fromIterable(properties).flatMapCompletable { property ->
                        propertySource.saveProperty(property)
                            .andThen(
                                if (property.photos.isNotEmpty()) {
                                    photoSource.savePhotos(property.photos)
                                } else {
                                    Completable.complete()
                                }
                            )
                    }
                }
            }
            Photo::class -> {
                value.filterIsInstance(Photo::class.java).let { photos ->
                    if (photos.isNotEmpty()) { photoSource.savePhotos(photos) } else { Completable.complete() }
                }
            }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> findById(type: KClass<T>, id: String): Single<T> {
        return when (type) {
            Property::class -> {
                propertySource.findPropertyById(id).flatMap { property ->
                    photoSource.findPhotosByPropertyId(property.id).flatMap { photos ->
                        property.photos.addAll(photos)
                        Single.just(property)
                    }
                } as Single<T>
            }
            Photo::class -> { photoSource.findPhotoById("", id) as Single<T> }
            else -> { Single.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> findAll(type: KClass<T>): Single<List<T>> {
        return when (type) {
            Property::class -> {
                if (propertySource is PropertyRemoteSource && photoSource is PhotoRemoteSource) {
                    propertySource.findAllProperties().flatMap { properties ->
                        Observable.fromIterable(properties).flatMapSingle { property ->
                            property.mainPhotoId?.let {
                                photoSource.findPhotoById(property.id, property.mainPhotoId!!).flatMap { photo ->
                                    property.photos.add(photo)
                                    Single.just(property)
                                }
                            } ?: Single.just(property)
                        }.toList().flatMap { Single.just(properties.sortedBy { it.id }) }
                    } as Single<List<T>>
                } else if (propertySource is PropertyCacheSource && photoSource is PhotoCacheSource) {
                    propertySource.findAllProperties().flatMap { properties ->
                        Observable.fromIterable(properties).flatMapSingle { property ->
                            photoSource.findPhotosByPropertyId(property.id).flatMap { photos ->
                                property.photos.addAll(photos)
                                Single.just(property)
                            }
                        }.toList().flatMap { Single.just(properties.sortedBy { it.id }) }
                    } as Single<List<T>>
                } else { Single.error(Throwable(ClassNotFoundException())) }
            }
            Photo::class -> { photoSource.findAllPhotos() as Single<List<T>> }
            else -> { Single.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> update(type: KClass<T>, value: T): Completable {
        return when (type) {
            Property::class -> { propertySource.updateProperty(value as Property) }
            Photo::class -> { photoSource.updatePhoto(value as Photo) }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> update(type: KClass<T>, value: List<T>): Completable {
        return when (type) {
            Property::class -> {
                value.filterIsInstance(Property::class.java).let { properties ->
                    Observable.fromIterable(properties).flatMapCompletable { property ->
                        propertySource.updateProperty(property)
                    }
                }
            }
            Photo::class -> {
                value.filterIsInstance(Photo::class.java).let { photos ->
                    if (photos.isNotEmpty()) { photoSource.updatePhotos(photos) } else { Completable.complete() }
                }
            }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> deleteAll(type: KClass<T>): Completable {
        return when (type) {
            Property::class -> { propertySource.deleteAllProperties() }
            Photo::class -> { photoSource.deleteAllPhotos() }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> delete(type: KClass<T>, value: T): Completable {
        return when (type) {
            Property::class -> { propertySource.deletePropertyById((value as Property).id) }
            Photo::class -> { photoSource.deletePhotoById((value as Property).id) }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }

    open fun <T : Any> delete(type: KClass<T>, value: List<T>): Completable {
        return when (type) {
            Property::class -> { propertySource.deleteProperties(value as List<Property>) }
            Photo::class -> { photoSource.deletePhotos(value as List<Photo>) }
            else -> { Completable.error(Throwable(ClassNotFoundException())) }
        }
    }
}
