package com.openclassrooms.realestatemanager.data.remote

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Flowable

interface PropertyApiService {

    fun allProperties(): Flowable<List<Property>>
}