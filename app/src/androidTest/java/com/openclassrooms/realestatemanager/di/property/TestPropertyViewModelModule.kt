package com.openclassrooms.realestatemanager.di.property

import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class TestPropertyViewModelModule {

    @Binds
    @BrowseScope
    abstract fun bindPropertiesViewModelFactory(factory: FakePropertiesViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @PropertyViewModelKey(PropertiesViewModel::class)
//    abstract fun bindPropertiesViewModel(propertiesViewModel: PropertiesViewModel): ViewModel
}