package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Observable
import org.apache.commons.lang3.tuple.MutablePair

interface PropertyRepository {

    fun findAllProperties(): Observable<MutablePair<Boolean?, MutableList<Property>?>>

    fun findProperty(propertyId: String): Observable<Property>

    fun updatePropertiesFromCache(): Observable<MutablePair<Boolean?, MutableList<Property>?>>

    fun updateProperty(propertyToUpdate: Property): Observable<Boolean>

}