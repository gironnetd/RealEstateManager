package com.openclassrooms.realestatemanager.data.remote

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Singleton

@Singleton
interface PropertyApiService {

    fun insertProperties(properties: List<Property>): Completable

    fun findAllProperties(): Flowable<List<Property>>
}