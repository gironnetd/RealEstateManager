package com.openclassrooms.realestatemanager.di.property.browse

import android.app.Application
import com.bumptech.glide.Glide
import com.openclassrooms.realestatemanager.data.remote.DefaultPropertyApiService
import com.openclassrooms.realestatemanager.repository.property.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.util.GlideManager
import com.openclassrooms.realestatemanager.util.GlideRequestManager
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
object BrowseModule {

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideGlideRequestManager(
            application: Application,
    ): GlideManager {
        return GlideRequestManager(
                Glide.with(application)
        )
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyRepository(apiService: DefaultPropertyApiService): PropertyRepository
        = DefaultPropertyRepository(apiService = apiService)
}