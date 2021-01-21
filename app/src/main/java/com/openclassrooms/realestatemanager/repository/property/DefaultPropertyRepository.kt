package com.openclassrooms.realestatemanager.repository.property

import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Flowable
import javax.inject.Inject

@BrowseScope
class DefaultPropertyRepository
@Inject
constructor(
        private val apiService: PropertyApiService,
) : PropertyRepository {

    override fun allProperties(): Flowable<List<Property>> {
        return Flowable.just(emptyList())
    }
}