package com.openclassrooms.realestatemanager.ui

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import junit.framework.TestCase

abstract class BaseMainActivityTests: TestCase() {

    val testApplication = InstrumentationRegistry.getInstrumentation()
        .targetContext
        .applicationContext as TestBaseApplication

    lateinit var mainActivity : FragmentActivity
    lateinit var propertiesRepository: FakePropertyRepository
    lateinit var fakeProperties: List<Property>

    open lateinit var apiService: FakePropertyApiService

    enum class ScreenSize { SMARTPHONE, PHABLET, TABLET, UNDEFINED }

    fun screen_size() : ScreenSize {
        val smallestScreen = testApplication.resources.configuration.smallestScreenWidthDp
        return when {
            smallestScreen >= 720 -> { TABLET }
            smallestScreen >= 600 -> { PHABLET }
            smallestScreen < 600 -> { SMARTPHONE }
            else -> UNDEFINED
        }
    }

    fun configure_fake_api_service(propertiesDataSource: String? = null, networkDelay: Long? = null)
    : FakePropertyApiService {
        apiService = (testApplication.browseComponent()).apiService
        propertiesDataSource?.let { apiService.propertiesJsonFileName = it }
        networkDelay?.let { apiService.networkDelay = it }
        return apiService
    }

    fun configure_fake_repository(apiService: FakePropertyApiService)/*: FakePropertyRepository*/ {
        propertiesRepository = (testApplication.browseComponent()).propertyRepository
        propertiesRepository.apiService = apiService
    }

    fun <T : Activity> ActivityScenario<T>.get_toolbar_navigation_content_description(): String {
        var description = ""
        onActivity { description = it.findViewById<Toolbar>(R.id.tool_bar)
                .navigationContentDescription as String
        }
        return description
    }

    abstract fun injectTest(application: TestBaseApplication)
}