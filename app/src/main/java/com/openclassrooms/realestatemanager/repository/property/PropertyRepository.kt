package com.openclassrooms.realestatemanager.repository.property

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Flowable

interface PropertyRepository {
    fun allProperties(): Flowable<List<Property>>
}