package com.openclassrooms.realestatemanager.data.remote.source

import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.realestatemanager.data.source.PropertyDataSource
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single

open class PropertyRemoteDataSource constructor(var firestore: FirebaseFirestore): PropertyDataSource {

    override fun count(): Single<Int> {
        TODO("Not yet implemented")
    }

    override fun saveProperty(property: Property): Completable {
        TODO("Not yet implemented")
    }

    override fun saveProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun findPropertyById(id: String): Single<Property> {
        TODO("Not yet implemented")
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        TODO("Not yet implemented")
    }

    override fun findAllProperties(): Single<List<Property>> {
        TODO("Not yet implemented")
    }

    override fun updateProperty(property: Property): Completable {
        TODO("Not yet implemented")
    }

    override fun updateProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteAllProperties(): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String): Completable {
        TODO("Not yet implemented")
    }
}