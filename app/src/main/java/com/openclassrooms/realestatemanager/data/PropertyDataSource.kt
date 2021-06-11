package com.openclassrooms.realestatemanager.data

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single

interface PropertyDataSource {

    fun count(): Single<Int>

    fun saveProperty(property: Property): Completable

    fun saveProperties(properties: List<Property>): Completable

    fun findPropertyById(id: String): Single<Property>

    fun findAllProperties(): Single<List<Property>>

    fun updateProperty(property: Property): Completable

    fun deleteAllProperties(): Completable

    fun deleteById(id: String): Completable
}