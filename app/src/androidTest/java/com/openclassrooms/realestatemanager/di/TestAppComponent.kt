package com.openclassrooms.realestatemanager.di

import android.app.Application
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.data.repository.FindAllPropertyRepositoryTest
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.data.repository.UpdatePropertyRepositoryTest
import com.openclassrooms.realestatemanager.di.property.TestBrowseComponent
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragmentTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.view.PhotoDetailDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.update.view.add.AddPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.update.view.update.PhotoUpdateDialogFragmentIntegrationTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            TestAppModule::class,
            AppFragmentModule::class,
            TestSubComponentsModule::class,
            TestViewModelModule::class
        ])
interface TestAppComponent : AppComponent {

    val propertyRepository: PropertyRepository

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): TestAppComponent
    }

    fun inject(listFragmentIntegrationTest: ListFragmentIntegrationTest)
    fun inject(mapFragmentIntegrationTest: MapFragmentIntegrationTest)
    fun inject(propertyDetailFragmentIntegrationTest: PropertyDetailFragmentIntegrationTest)
    fun inject(photoDetailDialogFragmentIntegrationTest: PhotoDetailDialogFragmentIntegrationTest)
    fun inject(updateFragmentIntegrationTest: PropertyUpdateFragmentIntegrationTest)
    fun inject(addPhotoDialogFragmentIntegrationTest: AddPhotoDialogFragmentIntegrationTest)
    fun inject(photoUpdateDialogFragmentIntegrationTest: PhotoUpdateDialogFragmentIntegrationTest)
    fun inject(mainActivityTest: MainActivityTest)
    fun inject(mainNavigationTest: MainNavigationTest)
    fun inject(mainRotationTest: MainRotationTest)
    fun inject(browseFragmentTest: BrowseFragmentTest)
    fun inject(propertyDaoTest: PropertyDaoTest)
    fun inject(appContentProviderTest: AppContentProviderTest)
    fun inject(connectivityManagerTest: ConnectivityManagerTest)
    fun inject(findAllPropertyRepositoryTest: FindAllPropertyRepositoryTest)
    fun inject(updatePropertyRepositoryTest: UpdatePropertyRepositoryTest)
    fun testBrowseComponent(): TestBrowseComponent.Factory
}