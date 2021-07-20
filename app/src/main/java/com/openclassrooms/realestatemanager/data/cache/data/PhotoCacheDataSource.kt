package com.openclassrooms.realestatemanager.data.cache.data

import com.openclassrooms.realestatemanager.data.cache.dao.PhotoDao
import com.openclassrooms.realestatemanager.data.cache.provider.toList
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class PhotoCacheDataSource
@Inject constructor(private val photoDao: PhotoDao): PhotoDataSource {

    override fun count(): Single<Int> {
        return Single.fromCallable { photoDao.count() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.fromCallable { photoDao.count(propertyId) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun savePhoto(photo: Photo): Completable {
        return Completable.fromAction {
            photoDao.savePhoto(photo = photo)
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Completable.fromAction {
            photoDao.savePhotos(*photos.toTypedArray())
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPhotoById(id: String): Single<Photo> {
        return Single.fromCallable {
            photoDao.findPhotoById(id).toList { Photo(it) }.single()
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return Single.fromCallable { photoDao.findPhotosByIds(ids) }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun findPhotosByPropertyId(propertyId: String): Single<List<Photo>> {
        TODO("Not yet implemented")
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return Single.fromCallable {
            photoDao.findAllPhotos().toList { Photo(it) }
        }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun findAllUpdatedPhotos(): Single<List<Photo>> {
        return Single.fromCallable {
            photoDao.findAllUpdatedPhotos().toList { Photo(it) }
        }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun updatePhoto(photo: Photo): Completable {
        return Completable.fromAction { photoDao.updatePhoto(photo) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return Completable.fromAction { photoDao.updatePhotos(*photos.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return Completable.fromAction { photoDao.deletePhotosByIds(ids) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return Completable.fromAction { photoDao.deletePhotos(*photos.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deleteAllPhotos(): Completable {
        return Completable.fromAction { photoDao.deleteAllPhotos() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePhotoById(id: String): Completable {
        return Completable.fromAction { photoDao.deletePhotoById(id) }
            .subscribeOn(SchedulerProvider.io())
    }
}