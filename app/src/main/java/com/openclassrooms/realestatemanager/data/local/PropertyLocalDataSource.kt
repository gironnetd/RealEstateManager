package com.openclassrooms.realestatemanager.data.local

import com.openclassrooms.realestatemanager.data.PropertyDataSource
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@BrowseScope
open class PropertyLocalDataSource
@Inject
constructor(val database: AppDatabase) : PropertyDataSource {

    override fun saveProperty(property: Property): Completable {
        return Completable.fromAction {
            database.propertyDao().saveProperty(property = property)
            database.pictureDao().savePictures(property.pictures)
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.fromAction {
            database.propertyDao().saveProperties(properties = properties)
            properties.forEach { property ->
                database.pictureDao().savePictures(property.pictures)
            }
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findAllProperties(): Single<List<Property>> {
        return Single.fromCallable {
           val properties: List<Property> = database.propertyDao().findAllProperties().toList { Property(it) }
            properties.forEach { property ->
                property.pictures.addAll(database.pictureDao().findPicturesByPropertyId(property.id).toList { Picture(it) })
            }
            properties
        }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun deleteAllProperties(): Completable {
        return Completable.fromAction { database.propertyDao().deleteAllProperties() }
                .subscribeOn(SchedulerProvider.io())
    }
}