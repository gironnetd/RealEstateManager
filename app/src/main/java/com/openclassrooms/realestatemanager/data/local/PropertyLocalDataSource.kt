package com.openclassrooms.realestatemanager.data.local

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.data.source.PropertyDataSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.models.storageUrl
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

@BrowseScope
open class PropertyLocalDataSource
@Inject
constructor(val database: AppDatabase, val context: Context) : PropertyDataSource {

    override fun count(): Single<Int> {
        return Single.fromCallable { database.propertyDao().count() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperty(property: Property): Completable {
        return Completable.fromAction {
            database.propertyDao().saveProperty(property = property)
            database.photoDao().savePhotos(*property.photos.toTypedArray())
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.fromAction {
            properties.forEach { property ->
                database.propertyDao().saveProperty(property)
                database.photoDao().savePhotos(*property.photos.toTypedArray())

                property.photos.forEach { photo ->
                    val localFile = File(photo.storageLocalDatabase(context, true))
                    if(!localFile.exists()) {
                        val gsReference = Firebase.storage.getReferenceFromUrl(photo.storageUrl(Firebase.storage.reference.bucket, isThumbnail = true))
                        gsReference.getFile(localFile)
                    }
                }
            }
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPropertyById(id: String): Single<Property> {
        return Single.fromCallable {
            database.propertyDao().findPropertyById(id).toList { Property(it) }.single()
        }.subscribeOn(SchedulerProvider.io())
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return Single.fromCallable { database.propertyDao().findPropertiesByIds(ids) }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }
    
    override fun findAllProperties(): Single<List<Property>> {
        return Single.fromCallable {
            val properties: List<Property> = database.propertyDao().findAllProperties().toList { Property(it) }
            properties.forEach { property ->
                val photos: List<Photo> = database.photoDao().findPhotosByPropertyId(property.id).toList { Photo(it) }
                property.photos.addAll(photos)
            }
            properties
        }.subscribeOn(SchedulerProvider.io()).flatMap {
            Single.just(it)
        }
    }

    override fun updateProperty(property: Property): Completable {
        return Completable.fromAction { database.propertyDao().updateProperty(property) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return Completable.fromAction { database.propertyDao().updateProperties(*properties.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        return Completable.fromAction { database.propertyDao().deletePropertiesByIds(ids) }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return Completable.fromAction { database.propertyDao().deleteProperties(*properties.toTypedArray()) }
            .subscribeOn(SchedulerProvider.io())
    }
    override fun deleteAllProperties(): Completable {
        return Completable.fromAction { database.propertyDao().deleteAllProperties() }
            .subscribeOn(SchedulerProvider.io())
    }

    override fun deletePropertyById(id: String): Completable {
        return Completable.fromAction { database.propertyDao().deletePropertyById(id) }
            .subscribeOn(SchedulerProvider.io())
    }
}