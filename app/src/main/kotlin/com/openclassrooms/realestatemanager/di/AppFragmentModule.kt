package com.openclassrooms.realestatemanager.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.ui.fragments.MainFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppFragmentModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideMainFragmentFactory(viewModelFactory: ViewModelProvider.Factory): FragmentFactory {
        return MainFragmentFactory(viewModelFactory = viewModelFactory, registry = null)
    }
}
