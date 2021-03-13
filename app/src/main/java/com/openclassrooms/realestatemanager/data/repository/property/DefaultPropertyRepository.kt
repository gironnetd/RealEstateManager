package com.openclassrooms.realestatemanager.data.repository.property

import com.openclassrooms.realestatemanager.data.local.PropertyLocalDataSource
import com.openclassrooms.realestatemanager.data.remote.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import io.reactivex.Observable
import javax.inject.Inject

@BrowseScope
class DefaultPropertyRepository
@Inject
constructor(
        val networkConnectionLiveData: NetworkConnectionLiveData,
        private val propertyRemoteDataSource: PropertyRemoteDataSource,
        private val propertyLocalDataSource: PropertyLocalDataSource
) : PropertyRepository {

    override fun findAllProperties(): Observable<List<Property>> {
        TODO("Not yet implemented")
    }
}