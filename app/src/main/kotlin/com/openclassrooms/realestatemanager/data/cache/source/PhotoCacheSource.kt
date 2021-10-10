package com.openclassrooms.realestatemanager.data.cache.source

import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoStorageSource
import com.openclassrooms.realestatemanager.models.property.Photo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoCacheSource
@Inject constructor(var cacheData: PhotoDataSource,
            var cacheStorage: PhotoStorageSource): PhotoSource {

    override fun count(): Single<Int> {
        return Single.zip(cacheData.count(), cacheStorage.count(),
            BiFunction { dataCount, storageCount ->
                if(dataCount == storageCount) {
                    return@BiFunction dataCount
                } else {
                    return@BiFunction -1
                }
            })
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.zip(cacheData.count(propertyId), cacheStorage.count(propertyId),
            BiFunction { dataCount, storageCount ->
                if(dataCount == storageCount) {
                    return@BiFunction dataCount
                } else {
                    return@BiFunction -1
                }
            })
    }

    override fun savePhoto(photo: Photo): Completable {
        return cacheData.savePhoto(photo).andThen(cacheStorage.savePhoto(photo))
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { savePhoto(photo) } ?: Completable.complete()
        }
    }

    override fun findPhotoById(propertyId: String, id: String): Single<Photo> {
        return cacheData.findPhotoById(id).flatMap { photo ->
            cacheStorage.findPhotoById(propertyId, id).flatMap { bitmap ->
                photo.bitmap = bitmap
                Single.just(photo)
            }
        }
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return Observable.fromIterable(ids).flatMapSingle { id ->
            findPhotoById("", id)
        }.toList().flatMap { photos -> Single.just(photos) }
    }

    override fun findPhotosByPropertyId(propertyId: String): Single<List<Photo>> {
        return cacheData.findPhotosByPropertyId(propertyId).flatMap { photos ->
            Observable.fromIterable(photos).flatMapSingle { photo ->
                cacheStorage.findPhotoById(photo.propertyId, photo.id).flatMap { bitmap ->
                    photo.bitmap = bitmap
                    Single.just(photo)
                }.onErrorReturn { photo }
            }.toList().flatMap {
                Single.just(it)
            }
        }
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return cacheData.findAllPhotos().flatMap { photos ->
            Observable.fromIterable(photos).flatMapSingle { photo ->
                cacheStorage.findPhotoById(photo.propertyId, photo.id).flatMap { bitmap ->
                    photo.bitmap = bitmap
                    Single.just(photo)
                }
            }.toList().flatMap { Single.just(it) }
        }
    }

    override fun updatePhoto(photo: Photo): Completable {
        return cacheData.updatePhoto(photo).andThen(cacheStorage.updatePhoto(photo))
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return cacheData.updatePhotos(photos).andThen(cacheStorage.updatePhotos(photos))
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return cacheData.deletePhotosByIds(ids).andThen(cacheStorage.deletePhotosByIds(ids))
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return cacheData.deletePhotos(photos).andThen(cacheStorage.deletePhotos(photos))
    }

    override fun deleteAllPhotos(): Completable {
        return cacheData.deleteAllPhotos().andThen(cacheStorage.deleteAllPhotos())
    }

    override fun deletePhotoById(id: String): Completable {
        return cacheData.deletePhotoById(id).andThen(cacheStorage.deletePhotoById(id))
    }
}