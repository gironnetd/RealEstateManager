package com.openclassrooms.realestatemanager.data.remote

import com.openclassrooms.realestatemanager.data.PropertyDataSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@BrowseScope
open class PropertyRemoteDataSource
@Inject
constructor(val apiService: PropertyApiService): PropertyDataSource {
    override fun count(): Int {
        TODO("Not yet implemented")
    }

    override fun saveProperty(property: Property): Completable {
        TODO("Not yet implemented")
    }

    override fun saveProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun findPropertyById(id: String): Single<Property> {
        TODO("Not yet implemented")
    }

    override fun findAllProperties(): Single<List<Property>> {
       return apiService.findAllProperties()
    }

    override fun updateProperty(property: Property): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteAllProperties(): Completable {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: String): Completable {
        TODO("Not yet implemented")
    }
}