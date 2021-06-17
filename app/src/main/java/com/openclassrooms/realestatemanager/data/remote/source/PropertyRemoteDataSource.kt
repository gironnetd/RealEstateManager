package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.remote.firestore.PropertyFirestoreFeature
import com.openclassrooms.realestatemanager.data.source.PropertyDataSource
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single

open class PropertyRemoteDataSource constructor(var propertyFirestore: PropertyFirestoreFeature): PropertyDataSource {

    override fun count(): Single<Int> {
       return propertyFirestore.count()
    }

    override fun saveProperty(property: Property): Completable {
        return propertyFirestore.saveProperty(property)
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return propertyFirestore.saveProperties(properties)
    }

    override fun findPropertyById(id: String): Single<Property> {
       return propertyFirestore.findPropertyById(id)
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return propertyFirestore.findPropertiesByIds(ids)
    }

    override fun findAllProperties(): Single<List<Property>> {
        return propertyFirestore.findAllProperties()
    }

    override fun updateProperty(property: Property): Completable {
        return propertyFirestore.updateProperty(property)
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return propertyFirestore.updateProperties(properties)
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
       return propertyFirestore.deletePropertiesByIds(ids)
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return propertyFirestore.deleteProperties(properties)
    }

    override fun deleteAllProperties(): Completable {
        return propertyFirestore.deleteAllProperties()
    }

    override fun deletePropertyById(id: String): Completable {
        return propertyFirestore.deletePropertyById(id)
    }


}