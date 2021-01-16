package com.openclassrooms.realestatemanager.di

import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.fragments.MainFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object FragmentModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideMainFragmentFactory(
    ): FragmentFactory {
        return MainFragmentFactory()
    }
}