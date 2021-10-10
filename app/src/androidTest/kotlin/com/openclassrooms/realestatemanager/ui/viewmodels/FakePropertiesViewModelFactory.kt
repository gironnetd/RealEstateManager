package com.openclassrooms.realestatemanager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateActionProcessor
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateViewModel
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateActionProcessor
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateViewModel
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesActionProcessor
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewModel
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailActionProcessor
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailViewModel
import com.openclassrooms.realestatemanager.util.schedulers.ImmediateSchedulerProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePropertiesViewModelFactory
@Inject constructor(private val propertiesRepository: PropertyRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesViewModel::class.java)) {
            val propertiesActionProcessor = PropertiesActionProcessor(propertiesRepository,
            ImmediateSchedulerProvider())
            return PropertiesViewModel(propertiesActionProcessor) as T
        }

        if (modelClass.isAssignableFrom(PropertyDetailViewModel::class.java)) {
            val propertiesActionProcessor = PropertyDetailActionProcessor(propertiesRepository,
                ImmediateSchedulerProvider())
            return PropertyDetailViewModel(propertiesActionProcessor) as T
        }

        if (modelClass.isAssignableFrom(PropertyUpdateViewModel::class.java)) {
            val propertiesActionProcessor = PropertyUpdateActionProcessor(propertiesRepository,
                ImmediateSchedulerProvider())
            return PropertyUpdateViewModel(propertiesActionProcessor) as T
        }

        if (modelClass.isAssignableFrom(PropertyCreateViewModel::class.java)) {
            val propertiesActionProcessor = PropertyCreateActionProcessor(propertiesRepository,
                ImmediateSchedulerProvider())
            return PropertyCreateViewModel(propertiesActionProcessor) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}