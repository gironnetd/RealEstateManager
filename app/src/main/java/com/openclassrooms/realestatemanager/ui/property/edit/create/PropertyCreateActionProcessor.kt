package com.openclassrooms.realestatemanager.ui.property.edit.create

import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import javax.inject.Inject

class PropertyCreateActionProcessor
@Inject constructor(private val propertyRepository: PropertyRepository,
                    private val schedulerProvider: BaseSchedulerProvider) {

}