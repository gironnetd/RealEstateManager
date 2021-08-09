package com.openclassrooms.realestatemanager.data.remote.source

import com.openclassrooms.realestatemanager.data.source.property.PropertyDataSource
import com.openclassrooms.realestatemanager.data.source.property.PropertySource
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRemoteSource
@Inject constructor(var remoteData: PropertyDataSource): PropertySource {

    override fun count(): Single<Int> {
       return remoteData.count()
    }

    override fun saveProperty(property: Property): Completable {
        return remoteData.saveProperty(property)
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return remoteData.saveProperties(properties)
    }

    override fun findPropertyById(id: String): Single<Property> {
       return remoteData.findPropertyById(id)
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return remoteData.findPropertiesByIds(ids)
    }

    override fun findAllProperties(): Single<List<Property>> {
        return remoteData.findAllProperties()
    }

    override fun findAllUpdatedProperties(): Single<List<Property>> {
        TODO("Not yet implemented")
    }

    override fun updateProperty(property: Property): Completable {
        return remoteData.updateProperty(property)
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return remoteData.updateProperties(properties)
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
       return remoteData.deletePropertiesByIds(ids)
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return remoteData.deleteProperties(properties)
    }

    override fun deleteAllProperties(): Completable {
        return remoteData.deleteAllProperties()
    }

    override fun deletePropertyById(id: String): Completable {
        return remoteData.deletePropertyById(id)
    }


}