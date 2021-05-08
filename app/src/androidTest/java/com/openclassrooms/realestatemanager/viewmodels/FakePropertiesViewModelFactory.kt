package com.openclassrooms.realestatemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesActionProcessor
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesViewModel
import com.openclassrooms.realestatemanager.util.schedulers.ImmediateSchedulerProvider
import javax.inject.Inject

@BrowseScope
class FakePropertiesViewModelFactory
@Inject
constructor(
        private val propertiesRepository: FakePropertyRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesViewModel::class.java)) {
            val propertiesActionProcessor = PropertiesActionProcessor(propertiesRepository,
            ImmediateSchedulerProvider())
            return PropertiesViewModel(propertiesActionProcessor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}