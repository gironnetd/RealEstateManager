package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.remote.data.PhotoRemoteDataSource
import com.openclassrooms.realestatemanager.data.remote.storage.PhotoRemoteStorageSource
import com.openclassrooms.realestatemanager.data.source.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Single

class PhotoRemoteSource
constructor(private val remoteData: PhotoRemoteDataSource, private val remoteStorage: PhotoRemoteStorageSource): PhotoDataSource {

    override fun count(): Single<Int> {
        return remoteData.count()
    }

    override fun count(propertyId: String): Single<Int> {
        return remoteData.count(propertyId)
    }

    override fun savePhoto(photo: Photo): Completable {
        return remoteData.savePhoto(photo)
    }

    override fun savePhotos(photos: List<Photo>): Completable {
       return remoteData.savePhotos(photos)
    }

    override fun findPhotoById(id: String): Single<Photo> {
        return remoteData.findPhotoById(id)
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return remoteData.findPhotosByIds(ids)
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return remoteData.findAllPhotos()
    }

    override fun updatePhoto(photo: Photo): Completable {
        return remoteData.updatePhoto(photo)
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return remoteData.updatePhotos(photos)
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return remoteData.deletePhotosByIds(ids)
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return remoteData.deletePhotos(photos)
    }

    override fun deleteAllPhotos(): Completable {
        return remoteData.deleteAllPhotos()
    }

    override fun deletePhotoById(id: String): Completable {
        return remoteData.deletePhotoById(id)
    }
}