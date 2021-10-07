package com.openclassrooms.realestatemanager.data.source.photo

import android.graphics.Bitmap
import com.openclassrooms.realestatemanager.models.property.Photo
import io.reactivex.Completable
import io.reactivex.Single

interface PhotoStorageSource {

    fun count(): Single<Int>

    fun count(propertyId: String): Single<Int>

    fun savePhoto(photo: Photo): Completable

    fun savePhotos(photos: List<Photo>): Completable

    fun findPhotoById(propertyId: String, id: String): Single<Bitmap>

    fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>>

    fun findPhotosByPropertyId(propertyId: String): Single<List<Bitmap>>

    fun findAllPhotos(): Single<List<Bitmap>>

    fun updatePhoto(photo: Photo): Completable

    fun updatePhotos(photos: List<Photo>): Completable

    fun deletePhotosByIds(ids: List<String>): Completable

    fun deletePhotos(photos: List<Photo>): Completable

    fun deleteAllPhotos(): Completable

    fun deletePhotoById(id: String): Completable
}