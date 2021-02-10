package com.openclassrooms.realestatemanager.repository.property

import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Flowable
import javax.inject.Inject

@BrowseScope
class FakePropertyRepository
@Inject
constructor(
        //var apiService: FakePropertyApiService
) : PropertyRepository {

    lateinit var apiService: FakePropertyApiService

    private fun throwExceptionIfApiServiceNotInitialized() {
        if (!::apiService.isInitialized) {
            throw UninitializedPropertyAccessException(
                    "Did you forget to set the ApiService in FakePropertyRepository"
            )
        }
    }

    @Throws(UninitializedPropertyAccessException::class)
    override fun allProperties(): Flowable<List<Property>> {
        throwExceptionIfApiServiceNotInitialized()
        return apiService.findAllProperties()
    }
}