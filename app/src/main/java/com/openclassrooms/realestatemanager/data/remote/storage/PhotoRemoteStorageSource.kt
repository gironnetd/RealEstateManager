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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutionException

class PhotoRemoteStorageSource constructor(private val storage: FirebaseStorage) {

    fun count(): Single<Int> { return findAllPhotos().map { photos -> photos.size } }

    fun count(propertyId: String): Single<Int> {
        return findPhotosByPropertyId(propertyId).map { photos -> photos.size }
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
        return findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMap {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMap {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMap { photoItem ->
                        if(photoItem.path.contains(id)) {
                            findPhotoByItem(photoItem)
                        } else {
                            Observable.empty()
                        }
                    }.toList().flatMap { photos ->
                        Single.just(photos[0])
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun findPhotosByIds(ids: List<String>): Single<List<Bitmap>> {
        return findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
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
                File.createTempFile("images", "jpg").let { localFile ->
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
        return findAllPropertiesPrefixes().flatMap { propertiesPrefixes ->
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
        return findPropertyPrefixById(propertyId).flatMap { propertyPrefix ->
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
        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMapCompletable {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMapCompletable {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
                        val match = ids.filter { it in photoItem.path }
                        if(match.isNotEmpty()) { deletePhotoByItem(photoItem) }
                        else { Completable.complete() }
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun deletePhotos(photos: List<Photo>): Completable {
        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMapCompletable {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMapCompletable {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
                        val match = photos.map { photo -> photo.id }.filter { it in photoItem.path }
                        if(match.isNotEmpty()) { deletePhotoByItem(photoItem) }
                        else { Completable.complete() }
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    fun deleteAllPhotos(): Completable {
        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMapCompletable {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMapCompletable {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
                        deletePhotoByItem(photoItem)
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }

    private fun deletePhotoByItem(item: StorageReference): Completable {
        return Completable.create { emitter ->
            try {
                Tasks.await(item.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful && task.isComplete) {
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
        return findAllPropertiesPrefixes().flatMapCompletable { propertiesPrefixes ->
            Observable.fromIterable(propertiesPrefixes).flatMap { propertyPrefix ->
                findPhotosPrefixesByProperty(propertyPrefix)
            }.toList().flatMapCompletable {
                val photosPrefixes = listOf(*it.toTypedArray()).flatten()
                Observable.fromIterable(photosPrefixes).flatMap { photoPrefix ->
                    findPhotosByPrefix(photoPrefix)
                }.toList().flatMapCompletable {
                    val photosItems = listOf(*it.toTypedArray()).flatten()
                    Observable.fromIterable(photosItems).flatMapCompletable { photoItem ->
                        if(photoItem.path.contains(id)) {
                            deletePhotoByItem(photoItem)
                        }
                        Completable.complete()
                    }.subscribeOn(SchedulerProvider.io())
                }.subscribeOn(SchedulerProvider.io())
            }.subscribeOn(SchedulerProvider.io())
        }.subscribeOn(SchedulerProvider.io())
    }
}
