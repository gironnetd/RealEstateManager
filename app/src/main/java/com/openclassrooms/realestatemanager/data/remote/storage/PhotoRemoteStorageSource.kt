package com.openclassrooms.realestatemanager.data.remote.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.storageUrl
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException

class PhotoRemoteStorageSource constructor(private val storage: FirebaseStorage) {

    var cachePhotos: MutableMap<String, Bitmap>? = null

    fun count(): Single<Int> {
        return cachePhotos?.let { Single.just(it.size) }
            ?: findAllPhotos().map { photos -> photos.size }
    }

    fun count(propertyId: String): Single<Int> {
        return cachePhotos?.let { cachePhotos ->
            Single.just(cachePhotos.filter { cachePhoto ->
                cachePhoto.key.contains(propertyId)
            }.values.size)
        } ?: findPhotosByPropertyId(propertyId).map { photos -> photos.size }
    }

    fun savePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            photo.bitmap?.let { bitmap ->
                val file = File.createTempFile("tmp_file_", ".png").apply {
                    createNewFile()
                    val outputStream = FileOutputStream(this, true)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                }

                val storageReference = storage.getReferenceFromUrl(photo.storageUrl(
                    storage.reference.bucket,
                    true)
                )

                storageReference.putFile(file.toUri(), storageMetadata { contentType = "image/jpeg" })
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful && task.isComplete) { emitter.onComplete() }
                    }.addOnFailureListener { emitter.onError(it) }
                    .also {
                        if(cachePhotos == null) { cachePhotos = ConcurrentHashMap() }
                        cachePhotos!![storageReference.path] = BitmapFactory.decodeFile(file.toString())
                        file.delete()
                    }
            } ?: emitter.onError(NullPointerException("bitmap for photo ${photo.id} is null"))
        }
    }

    fun savePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { savePhoto(photo) } ?: Completable.complete()
        }
    }

    fun findPhotoById(id: String): Single<Bitmap> {
        return cachePhotos?.let { cachePhotos ->
            Single.just(cachePhotos[cachePhotos.keys.single { key -> key.contains(id) }])
        } ?: findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMap {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMap {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMap { photoItem ->
                        //Timber.i("photoItem path: " + photoItem.path)
                        if(photoItem.path.contains(id)) {
                            Timber.i("photoItem path: " + photoItem.path)
                            Timber.i("PhotoRemoteStorage: findPhotoByItem: " + id)
                            findPhotoByItem(photoItem)
                        } else {
                            //Timber.tag(PhotoRemoteSource.TAG).i("PhotoRemoteStorage: empty: " + id)
                            Observable.empty()
                        }
                    }.toList().flatMap { photos ->
                        Timber.i("No photos: ${photos[0] == null}")
                        Single.just(photos[0])
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>> {
        return cachePhotos?.let { cachePhotos ->
            val photos = cachePhotos.toSortedMap().filter { cachePhoto -> ids.any { id -> cachePhoto.key.contains(id) } }
            Single.just(photos.toSortedMap().values.toList())
        } ?: findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMap {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMap {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMap { photoItem ->
                        val match = ids.filter { it in photoItem.path }
                        if(match.isNotEmpty()) { findPhotoByItem(photoItem) }
                        else { Observable.empty() }
                    }.toList().flatMap { bitmaps ->
                        Single.just(bitmaps)
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun findAllPropertiesPrefixes(): Single<List<StorageReference>> {
        return Single.create { emitter ->
            val propertiesRef = storage.reference.child(Constants.PROPERTIES_COLLECTION)
            try {
                Tasks.await(propertiesRef.listAll().addOnCompleteListener { propertiesTask ->
                    if (propertiesTask.isSuccessful && propertiesTask.isComplete) {
                        propertiesTask.result?.let { propertiesResult ->
                            emitter.onSuccess(propertiesResult.prefixes)
                        } ?: emitter.onError(NullPointerException("PropertiesTask Result is null"))
                    }
                })
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun findPhotosPrefixesByProperty(propertyPrefix: StorageReference): Observable<List<StorageReference>> {
        return Observable.create { emitter ->
            val photosRef = propertyPrefix.child(Constants.PHOTOS_COLLECTION)
            try {
                Tasks.await(photosRef.listAll().addOnCompleteListener { photosTask ->
                    if (photosTask.isSuccessful && photosTask.isComplete) {
                        photosTask.result?.let { photosResult ->
                            if(photosResult.prefixes.isNotEmpty()) {
                                emitter.onNext(photosResult.prefixes)
                            }
                            emitter.onComplete()
                        }
                    }
                }.addOnFailureListener { emitter.onError(it) })
            }  catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun findPhotosByPrefix(photoPrefix: StorageReference): Observable<List<StorageReference>> {
        return Observable.create { emitter ->
            try {
                Tasks.await(photoPrefix.listAll().addOnCompleteListener { photosTask ->
                    if (photosTask.isSuccessful && photosTask.isComplete) {
                        photosTask.result?.let { photosResult ->
                            if(photosResult.items.isNotEmpty()) {
                                emitter.onNext(photosResult.items)
                            }
                            emitter.onComplete()
                        }
                    }
                }.addOnFailureListener { emitter.onError(it) })
            }  catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun findPhotoByItem(item: StorageReference): Observable<Bitmap> {
        return Observable.create { emitter ->
            try {
                File.createTempFile("images", ".jpg").let { localFile ->
                    Tasks.await(item.getFile(localFile).addOnCompleteListener { task ->
                        if (task.isSuccessful && task.isComplete) {
                            BitmapFactory.decodeFile(localFile.toString()).also { bitmap ->
                                localFile.delete()
                                emitter.onNext(bitmap)
                                emitter.onComplete()
                            }
                        }
                    }.addOnFailureListener { emitter.onError(it) })
                }
            }  catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun findAllPhotos(): Single<List<Bitmap>> {
        return cachePhotos?.let { cachePhotos ->
            Single.just(cachePhotos.toSortedMap().values.toList())
        } ?:
        findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMap {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMap {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMap { photoItem ->
                        findPhotoByItem(photoItem)
                    }.toList().flatMap { bitmaps ->
                        Single.just(bitmaps)
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun findPropertyPrefixById(propertyId: String): Single<StorageReference> {
        return findAllPropertiesPrefixes().toObservable()
            .flatMapIterable { it }
            .filter { propertyPrefix -> propertyPrefix.name.contains(propertyId) }
            .singleOrError()
    }

    fun findPhotosByPropertyId(propertyId: String): Single<List<Bitmap>> {
        return cachePhotos?.let { cachePhotos ->
            Single.just(cachePhotos.filter { cachePhoto ->
                cachePhoto.key.contains(propertyId)
            }.values.toList())
        } ?:
        findPropertyPrefixById(propertyId).flatMap { propertyPrefix ->
            findPhotosPrefixesByProperty(propertyPrefix).toList().subscribeOn(SchedulerProvider.io())
        }.flatMap {
            val photosPrefixes = listOf(*it.toTypedArray()).flatten()
            Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                findPhotosByPrefix(photoPrefix)
            }.toList().flatMap {
                val photosItems = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosItems).flatMap { photoItem ->
                    findPhotoByItem(photoItem)
                }.toList().flatMap {
                    Single.just(it)
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun updatePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            photo.bitmap?.let { bitmap ->
                val file = File.createTempFile("tmp_file_", ".png").apply {
                    createNewFile()
                    val outputStream = FileOutputStream(this, true)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                }

                val storageReference = storage.getReferenceFromUrl(photo.storageUrl(
                    storage.reference.bucket,
                    true)
                )

                storageReference.putFile(file.toUri(), storageMetadata { contentType = "image/jpeg" })
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful && task.isComplete) { emitter.onComplete() }
                    }.addOnFailureListener { emitter.onError(it) }
                    .also {
                        cachePhotos!!.remove(photo.id)
                        cachePhotos!![storageReference.path] = BitmapFactory.decodeFile(file.toString())
                        file.delete()
                    }
            } ?: emitter.onError(NullPointerException("bitmap for photo ${photo.id} is null"))
        }
    }

    fun updatePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            photo.bitmap?.let { updatePhoto(photo) } ?: Completable.complete()
        }
    }

    fun deletePhotosByIds(ids: List<String>): Completable {
        return Observable.fromIterable(ids).flatMapCompletable { id ->
            deletePhotoById(id)
        }
//        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
//            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
//                findPhotosPrefixesByProperty(propertyPrefix)
//            }.toList().flatMapCompletable {
//                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
//                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
//                    findPhotosByPrefix(photoPrefix)
//                }.toList().flatMapCompletable {
//                    val photosItems = listOf(*it.toTypedArray()).flatten()
//                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
//                        val match = ids.filter { it in photoItem.path }
//                        if(match.isNotEmpty()) {
//                            cachePhotos!!.remove(match[0])
//                            deletePhotoByItem(photoItem)
//                        }
//                        else { Completable.complete() }
//                    }.subscribeOn(SchedulerProvider.io())
//                }.subscribeOn(SchedulerProvider.io())
//            }.subscribeOn(SchedulerProvider.io())
//        }.subscribeOn(SchedulerProvider.io())
    }

    fun deletePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            deletePhotoById(photo.id)
        }

//        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
//            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
//                findPhotosPrefixesByProperty(propertyPrefix)
//            }.toList().flatMapCompletable {
//                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
//                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
//                    findPhotosByPrefix(photoPrefix)
//                }.toList().flatMapCompletable {
//                    val photosItems = listOf(*it.toTypedArray()).flatten()
//                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
//                        val match = photos.map { photo -> photo.id }.filter { it in photoItem.path }
//                        if(match.isNotEmpty()) {
//                            cachePhotos!!.remove(match[0])
//                            deletePhotoByItem(photoItem)
//                        }
//                        else { Completable.complete() }
//                    }.subscribeOn(SchedulerProvider.io())
//                }.subscribeOn(SchedulerProvider.io())
//            }.subscribeOn(SchedulerProvider.io())
//        }.subscribeOn(SchedulerProvider.io())
    }

    fun deleteAllPhotos(): Completable {
        return Observable.fromIterable(cachePhotos!!.keys).flatMap { path ->
            Observable.just(storage.reference.child(path))
        }.toList().flatMapCompletable { photosRef ->
            Observable.fromIterable(photosRef).flatMapCompletable { photoItem ->
                deletePhotoByItem(photoItem)
            }.subscribeOn(SchedulerProvider.io())
        }
//        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
//            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
//                findPhotosPrefixesByProperty(propertyPrefix)
//            }.toList().flatMapCompletable {
//                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
//                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
//                    findPhotosByPrefix(photoPrefix)
//                }.toList().flatMapCompletable {
//                    val photosItems = listOf(*it.toTypedArray()).flatten()
//                    cachePhotos!!.clear()
//                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
//                        deletePhotoByItem(photoItem)
//                    }.subscribeOn(SchedulerProvider.io())
//                }.subscribeOn(SchedulerProvider.io())
//            }.subscribeOn(SchedulerProvider.io())
//        }.subscribeOn(SchedulerProvider.io())
    }

    private fun deletePhotoByItem(item: StorageReference): Completable {
        return Completable.create { emitter ->
            try {
                Tasks.await(item.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful && task.isComplete) {
                        cachePhotos!!.remove(cachePhotos!!.keys.filter { it in item.path }[0])
                        emitter.onComplete()
                    } else { task.exception?.let { exception -> emitter.onError(exception) } }
                }.addOnFailureListener { emitter.onError(it) })
            }  catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun deletePhotoById(id: String): Completable {
        return Observable.just(storage.reference.child(
                cachePhotos!!.keys.single { key -> key.contains(id) }
            )).toList().flatMapCompletable { photosRef ->
            Observable.fromIterable(photosRef).flatMapCompletable { photoItem ->
                deletePhotoByItem(photoItem)
            }.subscribeOn(SchedulerProvider.io())
        }

//        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
//            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
//                findPhotosPrefixesByProperty(propertyPrefix)
//            }.toList().flatMapCompletable {
//                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
//                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
//                    findPhotosByPrefix(photoPrefix)
//                }.toList().flatMapCompletable {
//                    val photosItems = listOf(*it.toTypedArray()).flatten()
//                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
//                        if(photoItem.path.contains(id)) {
//                            cachePhotos!!.remove(id)
//                            deletePhotoByItem(photoItem)
//                        }
//                        Completable.complete()
//                    }.subscribeOn(SchedulerProvider.io())
//                }.subscribeOn(SchedulerProvider.io())
//            }.subscribeOn(SchedulerProvider.io())
//        }.subscribeOn(SchedulerProvider.io())
    }
}
