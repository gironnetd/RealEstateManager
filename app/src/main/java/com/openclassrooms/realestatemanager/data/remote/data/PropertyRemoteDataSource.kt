package com.openclassrooms.realestatemanager.data.remote.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.openclassrooms.realestatemanager.data.source.property.PropertyDataSource
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRemoteDataSource
@Inject constructor(private var firestore: FirebaseFirestore): PropertyDataSource {

    override fun count(): Single<Int> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).get().addOnCompleteListener { task ->
                task.result?.let { result ->
                    emitter.onSuccess(result.count())
                }
                emitter.onSuccess(0)
            }
        }
    }

    override fun saveProperty(property: Property): Completable {
        return Completable.create { emitter ->
            val collectionRef = firestore.collection(Constants.PROPERTIES_COLLECTION).document(property.id)
            collectionRef.set(property).addOnSuccessListener {
                emitter.onComplete()
            }.addOnFailureListener {
                emitter.onError(it)
            }
        }
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.create { emitter ->
            val collectionRef = firestore.collection(Constants.PROPERTIES_COLLECTION)
            val batch = firestore.batch()

            for (property in properties) {
                val documentRef = collectionRef.document(property.id)
                batch.set(documentRef, property)
            }

            batch.commit().addOnCompleteListener { task ->
                if (task.isComplete && task.isSuccessful) {
                    emitter.onComplete()
                }
            }.addOnFailureListener { exception ->
                emitter.onError(exception)
            }
        }
    }

    override fun findPropertyById(id: String): Single<Property> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).document(id).get()
                .addOnSuccessListener { result ->
                    val property = result.toObject(Property::class.java)
                    if(property != null) {
                        emitter.onSuccess(property)
                    } else {
                        emitter.onError(FirebaseFirestoreException("Property by id: $id not found ", FirebaseFirestoreException.Code.NOT_FOUND))
                    }

                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return Single.create { emitter ->
            val properties: MutableList<Property> = arrayListOf()
            ids.forEach { id ->
                firestore.collection(Constants.PROPERTIES_COLLECTION).document(id).get()
                    .addOnSuccessListener { result ->
                        result.toObject(Property::class.java)?.let { property ->
                            properties.add(property)
                        }
                        if(id == ids.last()) {
                            emitter.onSuccess(properties)
                        }
                    }
            }
        }
    }

    override fun findAllProperties(): Single<List<Property>> {
        return Single.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION)
                .orderBy(Property.COLUMN_PROPERTY_ID, Query.Direction.ASCENDING)
                .get().addOnCompleteListener { task ->
                    if(task.isComplete &&  task.isSuccessful) {
                        task.result?.let { result ->
                            if(result.documents.isNotEmpty()) {
                                emitter.onSuccess(result.toObjects(Property::class.java))
                            } else {
                                emitter.onSuccess(emptyList())
                            }
                        } ?: emitter.onSuccess(emptyList())
                    } else {
                        emitter.onError(FirebaseFirestoreException("Find All Properties Aborted", FirebaseFirestoreException.Code.ABORTED))
                    }
                }.addOnFailureListener { exception -> emitter.onError(exception) }
        }
    }

    override fun findAllUpdatedProperties(): Single<List<Property>> {
        TODO("Not yet implemented")
    }

    override fun updateProperty(property: Property): Completable {
        return Completable.create { emitter ->
            firestore.runTransaction { transaction ->
                val documentRef = firestore.collection(Constants.PROPERTIES_COLLECTION).document(property.id)
                transaction.set(documentRef, property)
                null
            }.addOnCompleteListener { task ->
                if(task.isComplete &&  task.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(FirebaseFirestoreException("Property: ${property.id} Update Aborted", FirebaseFirestoreException.Code.ABORTED))
                }
            }.addOnFailureListener { exception -> emitter.onError(exception) }
        }
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return Completable.create { emitter ->
            properties.forEach { property ->
                firestore.collection(Constants.PROPERTIES_COLLECTION).document(property.id).set(property)
                if(property.id == properties.last().id) { emitter.onComplete() }
            }
        }
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        return Completable.create { emitter ->
            ids.forEach { id ->
                firestore.collection(Constants.PROPERTIES_COLLECTION).document(id).delete()
                if(id == ids.last()) { emitter.onComplete() }
            }
        }
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return Observable.fromIterable(properties).flatMapCompletable { property ->
            deletePropertyById(property.id)
        }
    }

    override fun deleteAllProperties(): Completable {
        return findAllProperties().flatMapCompletable { properties ->
            Observable.fromIterable(properties).flatMapCompletable { property ->
                deletePropertyById(property.id)
            }
        }
    }

    override fun deletePropertyById(id: String): Completable {
        return Completable.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).document(id).delete().addOnCompleteListener { task ->
                if(task.isComplete && task.isSuccessful) {
                    emitter.onComplete()
                }
            }.addOnFailureListener {
                emitter.onError(it)
            }
        }
    }
}