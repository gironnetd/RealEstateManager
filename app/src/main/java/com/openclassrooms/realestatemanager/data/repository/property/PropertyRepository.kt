package com.openclassrooms.realestatemanager.data.repository.property

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Observable

interface PropertyRepository {

    fun findAllProperties(): Observable<List<Property>>

    fun updateProperty(property: Property): Observable<Boolean>

}