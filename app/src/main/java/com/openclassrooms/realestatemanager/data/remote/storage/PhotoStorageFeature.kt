package com.openclassrooms.realestatemanager.data.remote.storage

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.models.Photo
import io.reactivex.Completable
import io.reactivex.Single

class PhotoStorageFeature constructor(private val storage: FirebaseStorage) {
     fun count(): Single<Int> {
        TODO("Not yet implemented")
    }

     fun count(propertyId: String): Single<Int> {
        TODO("Not yet implemented")
    }

     fun savePhoto(photo: Photo): Completable {
        TODO("Not yet implemented")
    }

     fun savePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

     fun findPhotoById(id: String): Single<Bitmap> {
        TODO("Not yet implemented")
    }

     fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

     fun findAllPhotos(): Single<List<Bitmap>> {
        TODO("Not yet implemented")
    }

     fun updatePhoto(photo: Photo): Completable {
        TODO("Not yet implemented")
    }

     fun updatePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

     fun deletePhotosByIds(ids: List<String>): Completable {
        TODO("Not yet implemented")
    }

     fun deletePhotos(photos: List<Photo>): Completable {
        TODO("Not yet implemented")
    }

     fun deleteAllPhotos(): Completable {
        TODO("Not yet implemented")
    }

     fun deletePhotoById(id: String): Completable {
        TODO("Not yet implemented")
    }

}
