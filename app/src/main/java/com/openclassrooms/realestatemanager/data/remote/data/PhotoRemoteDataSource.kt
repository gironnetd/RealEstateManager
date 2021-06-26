package com.openclassrooms.realestatemanager.data.remote.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class PhotoRemoteDataSource
constructor(private val firestore: FirebaseFirestore): PhotoDataSource {

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
            photos.filter { photo -> ids.contains(photo.id) }
        }
    }

    override fun findAllPhotos(): Single<List<Photo>> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).get()
                .addOnSuccessListener { result ->
                    val properties: List<Property> = result.toObjects(Property::class.java)
                    if(properties.isNotEmpty()) {
                        val photos: MutableList<Photo> = mutableListOf()
                        result.documents.forEach { document ->
                            document.reference.collection(Constants.PHOTOS_COLLECTION).get().addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    task.result?.let { result ->
                                        result.toObjects(Photo::class.java).forEach { photo ->
                                            photo.propertyId = document.id
                                            photos.add(photo)
                                        }
                                    } ?: emitter.onError(NullPointerException("No Photos for Property: ${document.id}"))
                                }
                                if(document.id == result.documents.last().id) {
                                    emitter.onSuccess(photos.sortedBy { it.id })
                                }
                            }
                        }
                    } else { emitter.onSuccess(emptyList()) }
                }
        }
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