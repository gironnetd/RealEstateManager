package com.openclassrooms.realestatemanager.di

import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.ui.viewmodels.FakePropertiesViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object TestViewModelModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideViewModelFactory(propertyRepository: PropertyRepository): ViewModelProvider.Factory {
        return FakePropertiesViewModelFactory(propertyRepository)
    }
}