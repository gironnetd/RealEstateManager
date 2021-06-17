package com.openclassrooms.realestatemanager.data.remote.source

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.data.source.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Single

class PhotoRemoteDataSource
constructor(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage?): PhotoDataSource {

    override fun count(): Single<Int> {
        TODO("Not yet implemented")
    }

    override fun count(propertyId: String): Single<Int> {
        TODO("Not yet implemented")
    }

    override fun savePhoto(photo: Photo): Completable {
        TODO("Not yet implemented")
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

    override fun findPhotoById(id: String): Single<Photo> {
        TODO("Not yet implemented")
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        TODO("Not yet implemented")
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        TODO("Not yet implemented")
    }

    override fun updatePhoto(photo: Photo): Completable {
        TODO("Not yet implemented")
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        TODO("Not yet implemented")
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteAllPhotos(): Completable {
        TODO("Not yet implemented")
    }

    override fun deletePhotoById(id: String): Completable {
        TODO("Not yet implemented")
    }

}