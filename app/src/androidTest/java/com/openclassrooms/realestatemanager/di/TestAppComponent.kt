package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.local.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.remote.PropertyApiServiceTest
import com.openclassrooms.realestatemanager.data.repository.property.BrowseFragmentTest
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepositoryTest
import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent
import com.openclassrooms.realestatemanager.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragmentIntegrationTest
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

    fun inject(listFragmentIntegrationTest: ListFragmentIntegrationTest)

    fun inject(mapFragmentIntegrationTest: MapFragmentIntegrationTest)

    fun inject(detailFragmentIntegrationTest: DetailFragmentIntegrationTest)

    fun inject(mainActivityTest: MainActivityTest)

    fun inject(mainNavigationTest: MainNavigationTest)

    fun inject(mainRotationTest: MainRotationTest)

    fun inject(browseFragmentTest: BrowseFragmentTest)

    fun inject(propertyDaoTest: PropertyDaoTest)

    fun inject(appContentProviderTest: AppContentProviderTest)

    fun inject(propertyApiServiceTest: PropertyApiServiceTest)

    fun inject(connectivityManagerTest: ConnectivityManagerTest)

    fun inject(propertyRepositoryTest: PropertyRepositoryTest)

    fun testBrowseComponent(): TestBrowseComponent.Factory
}