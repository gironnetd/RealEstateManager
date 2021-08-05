package com.openclassrooms.realestatemanager.data.remote.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRemoteDataSource
@Inject constructor(private val firestore: FirebaseFirestore): PhotoDataSource {

    override fun count(): Single<Int> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).get().addOnCompleteListener { task ->
                task.result?.let { result ->
                    if(result.documents.isNotEmpty()) {
                        var photosCount = 0
                        result.documents.forEach { document ->
                            document.reference.collection(Constants.PHOTOS_COLLECTION).get().addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    task.result?.let { result ->
                                        photosCount += result.count()
                                    }
                                }
                                if(document.id == result.documents.last().id) {
                                    emitter.onSuccess(photosCount)
                                }
                            }
                        }
                    } else {
                        emitter.onSuccess(0)
                    }
                } ?: emitter.onError(NullPointerException("No Properties Found"))
            }
        }
    }

    override fun count(propertyId: String): Single<Int> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).document(propertyId)
                .collection(Constants.PHOTOS_COLLECTION).get()
                .addOnCompleteListener { task ->
                    task.result?.let { result ->
                        emitter.onSuccess(result.count())
                    }
                    emitter.onSuccess(0)
                }
        }
    }

    override fun savePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            val collectionRef = firestore.collection(Constants.PROPERTIES_COLLECTION)
                .document(photo.propertyId).collection(Constants.PHOTOS_COLLECTION).document(photo.id)
            collectionRef.set(photo)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun savePhotos(photos: List<Photo>): Completable {
        return Completable.create { emitter ->
            val batch: WriteBatch = firestore.batch()
            photos.forEach { photo ->
                val documentRef = firestore
                    .collection(Constants.PROPERTIES_COLLECTION)
                    .document(photo.propertyId)
                    .collection(Constants.PHOTOS_COLLECTION)
                    .document(photo.id)
                batch.set(documentRef, photo)
            }

            batch.commit().addOnCompleteListener { task ->
                if (task.isComplete && task.isSuccessful) {
                    emitter.onComplete()
                }
            }.addOnFailureListener { exception -> emitter.onError(exception) }
        }
    }

    override fun findPhotoById(id: String): Single<Photo> {
        return findAllPhotos().map { photos -> photos.single { photo ->
            photo.id == id
        } }
    }

    override fun findPhotosByIds(ids: List<String>): Single<List<Photo>> {
        return findAllPhotos().map { photos ->
            photos.sortedBy { it.id }.filter { photo -> ids.contains(photo.id) }
        }
    }

    override fun findPhotosByPropertyId(propertyId: String): Single<List<Photo>> {
        return Single.create { emitter ->
            try {
                Tasks.await(firestore.collection(Constants.PROPERTIES_COLLECTION).document(propertyId)
                    .collection(Constants.PHOTOS_COLLECTION).get()
                    .addOnCompleteListener { task ->
                        task.result?.let { result ->
                            val photosByPropertyId = result.toObjects(Photo::class.java)
                            photosByPropertyId.forEach { photo -> photo.propertyId = propertyId }
                            emitter.onSuccess(photosByPropertyId)
                        } ?: emitter.onError(java.lang.NullPointerException("No Photos Found for property: ${propertyId}"))
                    }.addOnFailureListener { exception ->
                        emitter.onError(exception)
                    })
            }   catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return Single.create( SingleOnSubscribe<List<Property>> { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION)
                .orderBy(Property.COLUMN_PROPERTY_ID, Query.Direction.ASCENDING).get()
                .addOnSuccessListener { result ->
                    emitter.onSuccess(result.toObjects(Property::class.java))
                }
        }).flatMap { properties ->
            Observable.fromIterable(properties).flatMapSingle { property ->
                findPhotosByPropertyId(property.id)
            }.subscribeOn(SchedulerProvider.io()).toList().flatMap {
                val photos: List<Photo> = listOf(*it.toTypedArray()).flatten()
                Single.just(photos)
            }
        }
    }

    override fun findAllUpdatedPhotos(): Single<List<Photo>> {
        TODO("Not yet implemented")
    }

    override fun updatePhoto(photo: Photo): Completable {
        return Completable.create { emitter ->
            val documentRef = firestore
                .collection(Constants.PROPERTIES_COLLECTION)
                .document(photo.propertyId)
                .collection(Constants.PHOTOS_COLLECTION)
                .document(photo.id)
            documentRef.set(photo).addOnCompleteListener { task ->
                if (task.isComplete && task.isSuccessful) {
                    emitter.onComplete()
                }
            }.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun updatePhotos(photos: List<Photo>): Completable {
        return Observable.fromIterable(photos).flatMapCompletable { photo ->
            updatePhoto(photo)
        }
    }

    override fun deletePhotosByIds(ids: List<String>): Completable {
        return findPhotosByIds(ids).flatMapCompletable { photos ->
            Completable.create { emitter ->
                val batch: WriteBatch = firestore.batch()
                photos.filter { photo -> ids.contains(photo.id) }.forEach { photo ->
                    val documentRef = firestore
                        .collection(Constants.PROPERTIES_COLLECTION)
                        .document(photo.propertyId)
                        .collection(Constants.PHOTOS_COLLECTION)
                        .document(photo.id)
                    batch.delete(documentRef)
                }

                batch.commit().addOnCompleteListener { task ->
                    if (task.isComplete && task.isSuccessful) { emitter.onComplete() }
                }.addOnFailureListener { exception -> emitter.onError(exception) }
            }
        }
    }

    override fun deletePhotos(photos: List<Photo>): Completable {
        return Completable.create { emitter ->
            val batch: WriteBatch = firestore.batch()
            photos.forEach { photo ->
                val documentRef = firestore.collection(Constants.PROPERTIES_COLLECTION)
                    .document(photo.propertyId)
                    .collection(Constants.PHOTOS_COLLECTION)
                    .document(photo.id)
                batch.delete(documentRef)
            }

            batch.commit().addOnCompleteListener { task ->
                if (task.isComplete && task.isSuccessful) { emitter.onComplete() }
            }.addOnFailureListener { exception -> emitter.onError(exception) }
        }
    }

    override fun deleteAllPhotos(): Completable {
        return Completable.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).get().addOnSuccessListener {
                it?.let { result ->
                    if(result.documents.isNotEmpty()) {
                        result.documents.forEach { document ->
                            document.reference.collection(Constants.PHOTOS_COLLECTION).get().addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    task.result?.let { result ->
                                        if(result.documents.isNotEmpty()) {
                                            result.documents.forEach { document ->
                                                document.reference.delete()
                                            }
                                        }
                                    } ?: emitter.onError(NullPointerException("No Photos for Property: ${document.id}"))
                                }
                                if(document.id == result.documents.last().id) {
                                    emitter.onComplete()
                                }
                            }
                        }
                    } else { emitter.onComplete() }
                }
            }.addOnFailureListener { emitter.onError(it) }
        }
    }

    override fun deletePhotoById(id: String): Completable {
        return findPhotoById(id).flatMapCompletable { photo ->
            Completable.create { emitter ->
                firestore.collection(Constants.PROPERTIES_COLLECTION).document(photo.propertyId)
                    .collection(Constants.PHOTOS_COLLECTION)
                    .document(photo.id)
                    .delete().addOnCompleteListener { task ->
                        if (task.isComplete && task.isSuccessful) { emitter.onComplete() }
                    }.addOnFailureListener { exception -> emitter.onError(exception) }
            }
        }
    }
}