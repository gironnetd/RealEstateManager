package com.openclassrooms.realestatemanager.view.suites

import com.openclassrooms.realestatemanager.view.MainActivityTest
import com.openclassrooms.realestatemanager.view.MainNavigationTest
import com.openclassrooms.realestatemanager.view.MainRotationTest
import com.openclassrooms.realestatemanager.view.realestate.RealEstateFragmentTest
import com.openclassrooms.realestatemanager.view.realestate.RealEstateMasterFragmentTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class,
        RealEstateFragmentTest::class,
        RealEstateMasterFragmentTest::class
)
class RunAllTests