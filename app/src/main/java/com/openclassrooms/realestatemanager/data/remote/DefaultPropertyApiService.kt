package com.openclassrooms.realestatemanager.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

class DefaultPropertyApiService
@Inject
constructor(
        var firestore: FirebaseFirestore
) : PropertyApiService {

    override fun insertProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun findAllProperties(): Flowable<List<Property>> {
        TODO("Not yet implemented")
    }
}