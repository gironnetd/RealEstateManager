package com.openclassrooms.realestatemanager.data.local

import com.openclassrooms.realestatemanager.data.PropertyDataSource
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@BrowseScope
open class PropertyLocalDataSource
@Inject
constructor(val propertyDao: PropertyDao) : PropertyDataSource {

    override fun saveProperty(property: Property): Completable {
        return Completable.fromAction { propertyDao.saveProperty(property = property) }
                .subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.fromAction { propertyDao.saveProperties(properties = properties) }
                .subscribeOn(SchedulerProvider.io())
    }

    override fun findAllProperties(): Single<List<Property>> {
        return Single.fromCallable {
            propertyDao.findAllProperties().toList { Property(it) }
        }.subscribeOn(SchedulerProvider.io()).flatMap { Single.just(it) }
    }

    override fun deleteAllProperties(properties: List<Property>): Completable {
        return Completable.fromSingle<Int> { propertyDao.deleteAllProperties(properties = properties) }
                .subscribeOn(SchedulerProvider.io())
    }
}