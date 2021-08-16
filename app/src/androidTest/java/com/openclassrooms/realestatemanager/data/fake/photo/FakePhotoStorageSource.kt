package com.openclassrooms.realestatemanager.data.fake.photo

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.source.photo.PhotoStorageSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.storageUrl
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@BrowseScope
class FakePhotoStorageSource
@Inject constructor(var jsonUtil: JsonUtil): PhotoStorageSource {

    var photosJsonFileName: String = ConstantsTest.PHOTOS_DATA_FILENAME
    var photos: MutableMap<String, Bitmap> = ConcurrentHashMap()

    init {
        val rawJson = jsonUtil.readJSONFromAsset(photosJsonFileName)
        val jsonPhotos: List<Photo> = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

        jsonPhotos.forEach { photo ->
            photos[photo.storageUrl("default-bucket", true)] = BitmapUtil.bitmapFromAsset(
                InstrumentationRegistry.getInstrumentation().targetContext, photo.id)
        }
    }

    override fun count(): Single<Int> {
        return Single.just(photos.size)
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.just(photos.filter { photo -> photo.key.contains(propertyId) }.size)
    }

    override fun savePhoto(photo: Photo): Completable {
        return Completable.fromAction {
            photos[photo.storageUrl("default-bucket", true)] = photo.bitmap!! }
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            savePhoto(photo)
        }
    }

    override fun findPhotoById(propertyId: String, id: String): Single<Bitmap> {
        return Single.just(photos[photos.keys.single { key -> key.contains(id) }])
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>> {
        return Observable.fromIterable(ids).flatMapSingle { id ->
            findPhotoById("", id)
        }.toList().flatMap { photos ->
            Single.just(photos)
        }
    }

    override fun findPhotosByPropertyId(propertyId: String): Single<List<Bitmap>> {
        return Single.just(photos.filter { entry -> entry.key.contains(propertyId) }.values.toList())
    }

    override fun findAllPhotos(): Single<List<Bitmap>> {
       return Single.just(photos.values.toList())
    }

    override fun updatePhoto(photo: Photo): Completable {
        return Completable.fromAction {
            photos[photos.keys.single { key -> key.contains(photo.id) }] = photo.bitmap!!
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
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            deletePhotoById(photo.id)
        }
    }

    override fun deleteAllPhotos(): Completable {
        return Completable.fromAction { photos.clear() }
    }

    override fun deletePhotoById(id: String): Completable {
        return Completable.fromAction { photos.remove(photos.keys.single { key -> key.contains(id) }) }
    }
}