package com.openclassrooms.realestatemanager.data.fake.photo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FakePhotoDataSource
constructor(var jsonUtil: JsonUtil?): PhotoDataSource {

    var photosJsonFileName: String = ConstantsTest.PHOTOS_DATA_FILENAME
    var photos: ArrayList<Photo> = arrayListOf()

    init {
        val rawJson = jsonUtil!!.readJSONFromAsset(photosJsonFileName)
        photos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        photos.sortedBy { it.id }
    }

    override fun count(): Single<Int> {
        return Single.just(photos.size)
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.just(photos.filter { photo -> photo.propertyId == propertyId }.size)
    }

    override fun savePhoto(photo: Photo): Completable {
        return Completable.fromAction { photos.add(photo) }
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Completable.fromAction { this.photos.addAll(photos) }
    }

    override fun findPhotoById(id: String): Single<Photo> {
        return Single.just(photos.single { photo -> photo.id == id })
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return Observable.fromIterable(ids).flatMapSingle { id ->
            findPhotoById(id)
        }.toList().flatMap { photos ->
            Single.just(photos)
        }
    }

    override fun findPhotosByPropertyId(propertyId: String): Single<List<Photo>> {
        return Single.just(photos.filter { photo -> photo.propertyId == propertyId })
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return Single.just(photos)
    }

    override fun findAllUpdatedPhotos(): Single<List<Photo>> {
        return Single.just(photos.filter { photo -> photo.updated })
    }

    override fun updatePhoto(photo: Photo): Completable {
        return Completable.fromAction {
            val actualPhoto = photos.single { it.id == photo.id }
            photos[photos.indexOf(actualPhoto)] = photo
        }
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            updatePhoto(photo)
        }
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return Completable.fromAction { ids.forEach { id -> deletePhotoById(id) } }
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return Completable.fromAction { photos.forEach { photo -> this.photos.remove(photo) } }
    }

    override fun deleteAllPhotos(): Completable {
        return Completable.fromAction { photos.clear() }
    }

    override fun deletePhotoById(id: String): Completable {
        return Completable.fromAction { photos.remove(photos.single { photo -> photo.id == id }) }
    }
}