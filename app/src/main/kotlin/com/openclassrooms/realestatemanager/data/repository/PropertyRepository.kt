package com.openclassrooms.realestatemanager.data.repository

import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository.CreateOrUpdate
import com.openclassrooms.realestatemanager.models.property.Property
import io.reactivex.Observable

interface PropertyRepository {

    fun findAllProperties(): Observable<List<Property>>

    fun findProperty(propertyId: String): Observable<Property>

    fun updateProperty(updatedProperty: Property): Observable<Boolean>

    fun createProperty(createdProperty: Property): Observable<Boolean>

    fun pushLocalChanges(): Observable<Pair<CreateOrUpdate, List<Property>>>
}
