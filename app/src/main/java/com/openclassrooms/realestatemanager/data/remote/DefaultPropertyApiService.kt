package com.openclassrooms.realestatemanager.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction.ASCENDING
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PROPERTY_ID
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DefaultPropertyApiService
@Inject
constructor(
        var firestore: FirebaseFirestore
) : PropertyApiService {

    override fun insertProperties(properties: List<Property>): Completable {
        return Completable.unsafeCreate { emitter ->
            val collectionRef = firestore
                    .collection(PROPERTIES_COLLECTION)
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

    override fun findAllProperties(): Single<List<Property>> {
        return Single.create { emitter ->
           firestore.collection(PROPERTIES_COLLECTION)
                    .orderBy(COLUMN_PROPERTY_ID, ASCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        val properties = result.toObjects(Property::class.java)
                        emitter.onSuccess(properties)
                    }
        }
    }
}