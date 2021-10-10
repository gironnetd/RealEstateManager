package com.openclassrooms.realestatemanager.data.source.photo

import com.openclassrooms.realestatemanager.models.property.Photo
import io.reactivex.Completable
import io.reactivex.Single

interface PhotoDataSource {

    fun count(): Single<Int>

    fun count(propertyId: String): Single<Int>

    fun savePhoto(photo: Photo): Completable

    fun savePhotos(photos: List<Photo>): Completable

    fun findPhotoById(id: String): Single<Photo>

    fun findPhotosByIds(ids: List<String>): Single<List<Photo>>

    fun findPhotosByPropertyId(propertyId: String): Single<List<Photo>>

    fun findAllPhotos(): Single<List<Photo>>

    fun updatePhoto(photo: Photo): Completable

    fun updatePhotos(photos: List<Photo>): Completable

    fun deletePhotosByIds(ids: List<String>): Completable

    fun deletePhotos(photos: List<Photo>): Completable

    fun deleteAllPhotos(): Completable

    fun deletePhotoById(id: String): Completable
}