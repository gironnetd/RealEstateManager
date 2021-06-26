package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.remote.data.PhotoRemoteDataSource
import com.openclassrooms.realestatemanager.data.remote.storage.PhotoRemoteStorageSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import timber.log.Timber

class PhotoRemoteSource
constructor(private val remoteData: PhotoRemoteDataSource, private val remoteStorage: PhotoRemoteStorageSource):
    PhotoDataSource {

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

    override fun findPhotoById(id: String): Single<Photo> {
        return remoteData.findPhotoById(id).flatMap { photo ->
            remoteStorage.findPhotoById(id).flatMap { bitmap ->
                photo.bitmap = bitmap
                Single.just(photo)
            }
        }
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return Observable.fromIterable(ids).flatMapSingle { id ->
            findPhotoById(id)
        }.toList().flatMap { photos -> Single.just(photos) }
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return remoteData.findAllPhotos().flatMap { photos ->
            Timber.i("remoteData.findAllPhotos() size: " + photos.size)
            Observable.fromIterable(photos).flatMapSingle { photo ->
                synchronized(this) {
                    Timber.i("Photo id: " + photo.id)
                    remoteStorage.findPhotoById(photo.id).flatMap { bitmap ->
                        Timber.i("remoteStorage.findPhotoById: " + photo.id)
                        photo.bitmap = bitmap
                        Single.just(photo)
                    }
                }
            }.toList().flatMap {
                Timber.i("Photo without bitmap: " + it.filter { photo ->  photo.bitmap == null }.isNotEmpty())
                Timber.i("Return findAllPhotos size: " + it.size)
                Single.just(it) }
        }
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