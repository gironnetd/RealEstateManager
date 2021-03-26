package com.openclassrooms.realestatemanager.di.property

import com.codingwithmitch.espressodaggerexamples.util.FakeGlideRequestManager
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesActionProcessor
import com.openclassrooms.realestatemanager.util.GlideManager
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
object TestBrowseModule {

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideGlideRequestManager(
    ): GlideManager {
        return FakeGlideRequestManager()
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertiesActionProcessor(
            schedulerProvider: BaseSchedulerProvider,
            propertyRepository: FakePropertyRepository,
    ): PropertiesActionProcessor =
            PropertiesActionProcessor(
                    propertyRepository = propertyRepository,
                    schedulerProvider = schedulerProvider
            )
}