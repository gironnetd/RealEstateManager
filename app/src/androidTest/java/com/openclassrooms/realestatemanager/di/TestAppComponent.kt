package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.*
import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragmentTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.BrowseListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.BrowseMapFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.view.add.AddPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.view.update.PhotoUpdateDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.view.PhotoDetailDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.search.PropertySearchFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.setting.PropertyCurrencySettingIntegrationTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            TestAppModule::class,
            TestAppFragmentModule::class,
            TestViewModelModule::class,
            TestSubComponentsModule::class
        ])
interface TestAppComponent : AppComponent {

    val propertyRepository: PropertyRepository

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): TestAppComponent
    }

    fun inject(listFragmentIntegrationTest: BrowseListFragmentIntegrationTest)
    fun inject(mapFragmentIntegrationTest: BrowseMapFragmentIntegrationTest)
    fun inject(propertyDetailFragmentIntegrationTest: PropertyDetailFragmentIntegrationTest)
    fun inject(photoDetailDialogFragmentIntegrationTest: PhotoDetailDialogFragmentIntegrationTest)
    fun inject(updateFragmentIntegrationTest: PropertyUpdateFragmentIntegrationTest)
    fun inject(createFragmentIntegrationTest: PropertyCreateFragmentIntegrationTest)
    fun inject(addPhotoDialogFragmentIntegrationTest: AddPhotoDialogFragmentIntegrationTest)
    fun inject(photoUpdateDialogFragmentIntegrationTest: PhotoUpdateDialogFragmentIntegrationTest)
    fun inject(searchFragmentIntegrationTest: PropertySearchFragmentIntegrationTest)
    fun inject(propertyCurrencySettingIntegrationTest: PropertyCurrencySettingIntegrationTest)
    fun inject(mainActivityTest: MainActivityTest)
    fun inject(mainNavigationTest: MainNavigationTest)
    fun inject(mainRotationTest: MainRotationTest)
    fun inject(browseFragmentTest: BrowseFragmentTest)
    fun inject(propertyDaoTest: PropertyDaoTest)
    fun inject(appContentProviderTest: AppContentProviderTest)
    fun inject(connectivityManagerTest: ConnectivityManagerTest)
    fun inject(findAllPropertyRepositoryTest: FindAllPropertyRepositoryTest)
    fun inject(updatePropertyRepositoryTest: UpdatePropertyRepositoryTest)
    fun inject(createPropertyRepositoryTest: CreatePropertyRepositoryTest)
    fun testBrowseComponent(): TestBrowseComponent.Factory
}