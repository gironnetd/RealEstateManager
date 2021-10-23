package com.openclassrooms.realestatemanager.di

import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.ui.fragments.FakeMainFragmentFactory
import com.openclassrooms.realestatemanager.ui.viewmodels.FakePropertiesViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object TestAppFragmentModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideMainFragmentFactory(viewModelFactory: FakePropertiesViewModelFactory): FragmentFactory
            = FakeMainFragmentFactory(viewModelFactory = viewModelFactory, registry = null)
}