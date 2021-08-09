package com.openclassrooms.realestatemanager.ui.suites

import com.openclassrooms.realestatemanager.data.cache.dao.PhotoDaoTest
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.cache.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.data.repository.FindAllPropertyRepositoryTest
import com.openclassrooms.realestatemanager.data.repository.UpdatePropertyRepositoryTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragmentTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.view.add.AddPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.edit.view.update.PhotoUpdateDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.propertydetail.view.PhotoDetailDialogFragmentIntegrationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class,
        BrowseFragmentTest::class,
        ListFragmentIntegrationTest::class,
        MapFragmentIntegrationTest::class,
        PropertyDetailFragmentIntegrationTest::class,
        PhotoDetailDialogFragmentIntegrationTest::class,
        PropertyUpdateFragmentIntegrationTest::class,
        PropertyCreateFragmentIntegrationTest::class,
        AddPhotoDialogFragmentIntegrationTest::class,
        PhotoUpdateDialogFragmentIntegrationTest::class,
        PropertyDaoTest::class,
        PhotoDaoTest::class,
        AppContentProviderTest::class,
        ConnectivityManagerTest::class,
        FindAllPropertyRepositoryTest::class,
        UpdatePropertyRepositoryTest::class
)
class RunAllTests