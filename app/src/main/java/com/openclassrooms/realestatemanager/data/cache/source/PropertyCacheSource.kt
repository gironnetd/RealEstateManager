package com.openclassrooms.realestatemanager.data.cache.source

import com.openclassrooms.realestatemanager.data.source.property.PropertyDataSource
import com.openclassrooms.realestatemanager.data.source.property.PropertySource
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PropertyCacheSource
@Inject constructor(var cacheData: PropertyDataSource): PropertySource {

    override fun count(): Single<Int> {
        return cacheData.count()
    }

    override fun saveProperty(property: Property): Completable {
        return cacheData.saveProperty(property)
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return cacheData.saveProperties(properties)
    }

    override fun findPropertyById(id: String): Single<Property> {
        return cacheData.findPropertyById(id)
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return cacheData.findPropertiesByIds(ids)
    }

    override fun findAllProperties(): Single<List<Property>> {
        return cacheData.findAllProperties()
    }

    override fun findAllUpdatedProperties(): Single<List<Property>> {
        return cacheData.findAllUpdatedProperties()
    }

    override fun updateProperty(property: Property): Completable {
        return cacheData.updateProperty(property)
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return cacheData.updateProperties(properties)
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        return cacheData.deletePropertiesByIds(ids)
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return cacheData.deleteProperties(properties)
    }

    override fun deleteAllProperties(): Completable {
        return cacheData.deleteAllProperties()
    }

    override fun deletePropertyById(id: String): Completable {
        return cacheData.deletePropertyById(id)
    }
}