package com.openclassrooms.realestatemanager.data.cache.data

import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.cache.provider.toList
import com.openclassrooms.realestatemanager.data.source.PropertyDataSource
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single

class PropertyCacheDataSource constructor(private val propertyDao: PropertyDao) :
    PropertyDataSource {

    override fun count(): Single<Int> {
        return Single.fromCallable { propertyDao.count() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperty(property: Property): Completable {
        return Completable.fromAction {
            propertyDao.saveProperty(property = property)
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.fromAction {
            properties.forEach { property -> propertyDao.saveProperty(property) }
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPropertyById(id: String): Single<Property> {
        return Single.fromCallable {
            propertyDao.findPropertyById(id).toList { Property(it) }.single()
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return Single.fromCallable { propertyDao.findPropertiesByIds(ids) }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }
    
    override fun findAllProperties(): Single<List<Property>> {
        return Single.fromCallable {
            propertyDao.findAllProperties().toList { Property(it) }
        }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun updateProperty(property: Property): Completable {
        return Completable.fromAction { propertyDao.updateProperty(property) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return Completable.fromAction { propertyDao.updateProperties(*properties.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        return Completable.fromAction { propertyDao.deletePropertiesByIds(ids) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return Completable.fromAction { propertyDao.deleteProperties(*properties.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }
    override fun deleteAllProperties(): Completable {
        return Completable.fromAction { propertyDao.deleteAllProperties() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePropertyById(id: String): Completable {
        return Completable.fromAction { propertyDao.deletePropertyById(id) }
            .subscribeOn(SchedulerProvider.io())
    }
}