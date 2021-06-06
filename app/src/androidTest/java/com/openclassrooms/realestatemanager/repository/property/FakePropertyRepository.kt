package com.openclassrooms.realestatemanager.repository.property

import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.models.Property
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePropertyRepository
@Inject
constructor() : PropertyRepository {

    lateinit var apiService: FakePropertyApiService

    private fun throwExceptionIfApiServiceNotInitialized() {
        if (!::apiService.isInitialized) {
            throw UninitializedPropertyAccessException(
                    "Did you forget to set the ApiService in FakePropertyRepository"
            )
        }
    }

    @Throws(UninitializedPropertyAccessException::class)
    override fun findAllProperties(): Observable<List<Property>> {
        throwExceptionIfApiServiceNotInitialized()
        return apiService.findAllProperties().toObservable()
    }
}