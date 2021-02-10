package com.openclassrooms.realestatemanager.data.remote

import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Flowable
import javax.inject.Inject

@BrowseScope
class DefaultPropertyApiService
@Inject
constructor() : PropertyApiService {

    override fun findAllProperties(): Flowable<List<Property>> {
        TODO("Not yet implemented")
    }
}