package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.remote.data.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.data.source.PropertyDataSource
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single

open class PropertyRemoteSource constructor(var propertyRemoteDataSource: PropertyRemoteDataSource): PropertyDataSource {

    override fun count(): Single<Int> {
       return propertyRemoteDataSource.count()
    }

    override fun saveProperty(property: Property): Completable {
        return propertyRemoteDataSource.saveProperty(property)
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return propertyRemoteDataSource.saveProperties(properties)
    }

    override fun findPropertyById(id: String): Single<Property> {
       return propertyRemoteDataSource.findPropertyById(id)
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return propertyRemoteDataSource.findPropertiesByIds(ids)
    }

    override fun findAllProperties(): Single<List<Property>> {
        return propertyRemoteDataSource.findAllProperties()
    }

    override fun updateProperty(property: Property): Completable {
        return propertyRemoteDataSource.updateProperty(property)
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return propertyRemoteDataSource.updateProperties(properties)
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
       return propertyRemoteDataSource.deletePropertiesByIds(ids)
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return propertyRemoteDataSource.deleteProperties(properties)
    }

    override fun deleteAllProperties(): Completable {
        return propertyRemoteDataSource.deleteAllProperties()
    }

    override fun deletePropertyById(id: String): Completable {
        return propertyRemoteDataSource.deletePropertyById(id)
    }


}