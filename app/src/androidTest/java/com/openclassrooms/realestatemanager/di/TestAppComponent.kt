package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent
import com.openclassrooms.realestatemanager.repository.property.BrowseFragmentTest
import com.openclassrooms.realestatemanager.repository.property.BrowseMasterFragmentTest
import com.openclassrooms.realestatemanager.repository.property.properties.PropertiesFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            TestAppModule::class,
            AppFragmentModule::class,
            TestSubComponentsModule::class
        ])
interface TestAppComponent : AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): TestAppComponent
    }

    fun inject(browseFragmentIntegrationTest: PropertiesFragmentIntegrationTest)

    fun inject(mainActivityTest: MainActivityTest)

    fun inject(mainNavigationTest: MainNavigationTest)

    fun inject(mainRotationTest: MainRotationTest)

    fun inject(browseFragmentTest: BrowseFragmentTest)

    fun inject(browseMasterFragmentTest: BrowseMasterFragmentTest)

    fun testBrowseComponent(): TestBrowseComponent.Factory
}