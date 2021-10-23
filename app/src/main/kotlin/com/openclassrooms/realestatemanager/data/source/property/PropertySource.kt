package com.openclassrooms.realestatemanager.data.source.property

import com.openclassrooms.realestatemanager.models.property.Property
import io.reactivex.Completable
import io.reactivex.Single

interface PropertySource {

    fun count(): Single<Int>

    fun saveProperty(property: Property): Completable

    fun saveProperties(properties: List<Property>): Completable

    fun findPropertyById(id: String): Single<Property>

    fun findPropertiesByIds(ids: List<String>): Single<List<Property>>

    fun findAllProperties(): Single<List<Property>>

    fun updateProperty(property: Property): Completable

    fun updateProperties(properties: List<Property>): Completable

    fun deletePropertiesByIds(ids: List<String>): Completable

    fun deleteProperties(properties: List<Property>): Completable

    fun deleteAllProperties(): Completable

    fun deletePropertyById(id: String): Completable
}
