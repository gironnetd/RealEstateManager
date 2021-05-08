package com.openclassrooms.realestatemanager.data

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single

interface PropertyDataSource {

    fun saveProperty(property: Property): Completable

    fun saveProperties(properties: List<Property>): Completable

    fun findAllProperties(): Single<List<Property>>

    fun deleteAllProperties(): Completable
}