package com.openclassrooms.realestatemanager.data.remote

import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
interface PropertyApiService {

    fun saveProperties(properties: List<Property>): Completable

    fun findAllProperties(): Single<List<Property>>
}