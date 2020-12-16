package com.openclassrooms.realestatemanager.view.suites

import com.openclassrooms.realestatemanager.view.MainActivityTest
import com.openclassrooms.realestatemanager.view.MainNavigationTest
import com.openclassrooms.realestatemanager.view.MainRotationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        MainActivityTest::class,
        MainNavigationTest::class,
        MainRotationTest::class
)
class RunAllTests