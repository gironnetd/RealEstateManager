package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Observable

interface PropertyRepository {

    fun findAllProperties(): Observable<List<Property>>

    fun findProperty(propertyId: String): Observable<Property>

    fun updateProperty(updatedProperty: Property): Observable<Boolean>

    fun createProperty(createdProperty: Property): Observable<Boolean>

    fun saveRemotelyLocalChanges(updates: Boolean = false, creations: Boolean = false): Observable<List<Property>>
}