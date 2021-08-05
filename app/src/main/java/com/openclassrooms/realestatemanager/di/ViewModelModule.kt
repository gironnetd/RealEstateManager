package com.openclassrooms.realestatemanager.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.keys.PropertyViewModelKey
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewModel
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailViewModel
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateViewModel
import com.openclassrooms.realestatemanager.viewmodels.PropertiesViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindPropertiesViewModelFactory(factory: PropertiesViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @PropertyViewModelKey(PropertiesViewModel::class)
    abstract fun bindPropertiesViewModel(propertiesViewModel: PropertiesViewModel): ViewModel

    @Binds
    @IntoMap
    @PropertyViewModelKey(PropertyDetailViewModel::class)
    abstract fun bindPropertyDetailViewModel(propertyDetailViewModel: PropertyDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @PropertyViewModelKey(PropertyUpdateViewModel::class)
    abstract fun bindUpdatePropertyViewModel(propertyUpdateViewModel: PropertyUpdateViewModel): ViewModel
}