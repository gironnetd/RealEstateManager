package com.openclassrooms.realestatemanager.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import io.reactivex.Completable
import io.reactivex.Flowable
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
                    .collection(Constants.PROPERTIES_COLLECTION)
            val batch = firestore.batch()

            for (property in properties) {
                val documentRef = collectionRef.document(property.propertyId)
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

    override fun findAllProperties(): Flowable<List<Property>> {
        return Single.create<List<Property>> { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val properties = querySnapshot.toObjects(Property::class.java)
                        emitter.onSuccess(properties)
                    }
        }.toFlowable()
    }
}