package com.openclassrooms.realestatemanager.data.remote.source

import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.data.remote.firestore.PhotoFirestoreFeature
import com.openclassrooms.realestatemanager.data.source.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Single

class PhotoRemoteDataSource
constructor(private val photoFirestore: PhotoFirestoreFeature, private val storage: FirebaseStorage?): PhotoDataSource {

    override fun count(): Single<Int> {
        return photoFirestore.count()
    }

    override fun count(propertyId: String): Single<Int> {
        return photoFirestore.count(propertyId)
    }

    override fun savePhoto(photo: Photo): Completable {
        return photoFirestore.savePhoto(photo)
    }

    override fun savePhotos(photos: List<Photo>): Completable {
       return photoFirestore.savePhotos(photos)
    }

    override fun findPhotoById(id: String): Single<Photo> {
        return photoFirestore.findPhotoById(id)
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return photoFirestore.findPhotosByIds(ids)
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return photoFirestore.findAllPhotos()
    }

    override fun updatePhoto(photo: Photo): Completable {
        return photoFirestore.updatePhoto(photo)
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return photoFirestore.updatePhotos(photos)
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return photoFirestore.deletePhotosByIds(ids)
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return photoFirestore.deletePhotos(photos)
    }

    override fun deleteAllPhotos(): Completable {
        return photoFirestore.deleteAllPhotos()
    }

    override fun deletePhotoById(id: String): Completable {
        return photoFirestore.deletePhotoById(id)
    }
}