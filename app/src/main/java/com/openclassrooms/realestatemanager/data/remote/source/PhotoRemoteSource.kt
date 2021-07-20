package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoStorageSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@BrowseScope
open class PhotoRemoteSource
@Inject
constructor(var remoteData: PhotoDataSource,
            var remoteStorage: PhotoStorageSource
): PhotoSource {

    override fun count(): Single<Int> {
        return Single.zip(remoteData.count(), remoteStorage.count(),
            BiFunction { dataCount, storageCount ->
                if(dataCount == storageCount) {
                    return@BiFunction dataCount
                } else {
                    return@BiFunction -1
                }
            })
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.zip(remoteData.count(propertyId), remoteStorage.count(propertyId),
            BiFunction { dataCount, storageCount ->
                if(dataCount == storageCount) {
                    return@BiFunction dataCount
                } else {
                    return@BiFunction -1
                }
            })
    }

    override fun savePhoto(photo: Photo): Completable {
        return remoteData.savePhoto(photo).andThen(remoteStorage.savePhoto(photo))
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { savePhoto(photo) } ?: Completable.complete()
        }
    }

    override fun findPhotoById(propertyId: String, id: String): Single<Photo> {
        return remoteData.findPhotoById(id).flatMap { photo ->
            remoteStorage.findPhotoById(propertyId, id).flatMap { bitmap ->
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
        return remoteData.findPhotosByPropertyId(propertyId).flatMap { photos ->
            Observable.fromIterable(photos).flatMap { photo ->
                remoteStorage.findPhotoById(photo.propertyId, photo.id).flatMapObservable { bitmap ->
                    photo.bitmap = bitmap
                    Observable.just(photo)
                }
            }.toList().flatMap { Single.just(it) }
        }
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return remoteData.findAllPhotos().flatMap { photos ->
            remoteStorage.findAllPhotos().flatMap {
//                for((key, bitmap) in remoteStorage.cachePhotos!!) {
//                    photos.single { photo -> key.contains(photo.id) }.bitmap = bitmap
//                }
               Single.just(photos)
            }
//            Observable.fromIterable(photos).flatMapSingle { photo ->
//                synchronized(this) {
//                    remoteStorage.findPhotoById(photo.id).flatMap { bitmap ->
//                        photo.bitmap = bitmap
//                        Single.just(photo)
//                    }
//                }
//            }.toList().flatMap {
//                Single.just(it)
//            }
        }
    }

    override fun findAllUpdatedPhotos(): Single<List<Photo>> {
        TODO("Not yet implemented")
    }

    override fun updatePhoto(photo: Photo): Completable {
        return remoteData.updatePhoto(photo).andThen(remoteStorage.updatePhoto(photo))
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return remoteData.updatePhotos(photos).andThen(remoteStorage.updatePhotos(photos))
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return remoteData.deletePhotosByIds(ids).andThen(remoteStorage.deletePhotosByIds(ids))
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return remoteData.deletePhotos(photos).andThen(remoteStorage.deletePhotos(photos))
    }

    override fun deleteAllPhotos(): Completable {
        return remoteData.deleteAllPhotos().andThen(remoteStorage.deleteAllPhotos())
    }

    override fun deletePhotoById(id: String): Completable {
        return remoteData.deletePhotoById(id).andThen(remoteStorage.deletePhotoById(id))
    }
}