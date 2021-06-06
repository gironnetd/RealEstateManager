package com.openclassrooms.realestatemanager.di.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.di.property.keys.PropertyViewModelKey
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesViewModel
import com.openclassrooms.realestatemanager.viewmodels.PropertiesViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class PropertyViewModelModule {

    @Binds
    @BrowseScope
    abstract fun bindPropertiesViewModelFactory(factory: PropertiesViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @PropertyViewModelKey(PropertiesViewModel::class)
    abstract fun bindPropertiesViewModel(propertiesViewModel: PropertiesViewModel): ViewModel
}