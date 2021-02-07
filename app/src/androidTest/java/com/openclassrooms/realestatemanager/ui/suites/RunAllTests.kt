package com.openclassrooms.realestatemanager.ui.suites

import com.openclassrooms.realestatemanager.data.local.dao.PropertyDaoTest
import com.openclassrooms.realestatemanager.repository.property.BrowseFragmentTest
import com.openclassrooms.realestatemanager.repository.property.BrowseMasterFragmentTest
import com.openclassrooms.realestatemanager.repository.property.properties.PropertiesFragmentIntegrationTest
import com.openclassrooms.realestatemanager.ui.MainActivityTest
import com.openclassrooms.realestatemanager.ui.MainNavigationTest
import com.openclassrooms.realestatemanager.ui.MainRotationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class,
        BrowseFragmentTest::class,
        BrowseMasterFragmentTest::class,
        PropertiesFragmentIntegrationTest::class,
        PropertyDaoTest::class
)
class RunAllTests