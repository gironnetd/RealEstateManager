package com.openclassrooms.realestatemanager.ui.suites

import com.openclassrooms.realestatemanager.data.local.dao.PictureDaoTest
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.data.local.provider.AppContentProviderTest
import com.openclassrooms.realestatemanager.data.repository.property.BrowseFragmentTest
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepositoryTest
import com.openclassrooms.realestatemanager.repository.ConnectivityManagerTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragmentIntegrationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class,
        BrowseFragmentTest::class,
        ListFragmentIntegrationTest::class,
        PropertyDaoTest::class,
        PictureDaoTest::class,
        AppContentProviderTest::class,
        ConnectivityManagerTest::class,
        PropertyRepositoryTest::class,
        MapFragmentIntegrationTest::class,
        DetailFragmentIntegrationTest::class
)
class RunAllTests