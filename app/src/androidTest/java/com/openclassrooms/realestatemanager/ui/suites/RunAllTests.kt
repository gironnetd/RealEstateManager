package com.openclassrooms.realestatemanager.ui.suites

import com.openclassrooms.realestatemanager.data.local.dao.PhotoDaoTest
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.local.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.property.BrowseFragmentTest
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepositoryTest
import com.openclassrooms.realestatemanager.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.detail.view.PhotoDetailDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.update.UpdateFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.update.view.add.AddPhotoDialogFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.update.view.update.PhotoUpdateDialogFragmentIntegrationTest
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
        DetailFragmentIntegrationTest::class,
        PhotoDetailDialogFragmentIntegrationTest::class,
        UpdateFragmentIntegrationTest::class,
        AddPhotoDialogFragmentIntegrationTest::class,
        PhotoUpdateDialogFragmentIntegrationTest::class,
        PropertyDaoTest::class,
        PhotoDaoTest::class,
        AppContentProviderTest::class,
        ConnectivityManagerTest::class,
        PropertyRepositoryTest::class
)
class RunAllTests