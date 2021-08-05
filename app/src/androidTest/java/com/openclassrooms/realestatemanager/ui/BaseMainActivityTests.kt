package com.openclassrooms.realestatemanager.ui

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoDataSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoStorageSource
import com.openclassrooms.realestatemanager.data.fake.property.FakePropertyDataSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase

abstract class BaseMainActivityTests: TestCase() {

    val testApplication = InstrumentationRegistry.getInstrumentation()
        .targetContext
        .applicationContext as TestBaseApplication

    lateinit var mainActivity : FragmentActivity
    lateinit var propertiesRepository: PropertyRepository
    lateinit var fakeProperties: List<Property>
    var jsonUtil: JsonUtil = JsonUtil()

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

    fun configure_fake_repository() {
        ((testApplication.appComponent as TestAppComponent).propertyRepository as DefaultPropertyRepository).cacheDataSource =
            DataSource(
                propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
                photoSource = PhotoCacheSource(
                    cacheData = FakePhotoDataSource(jsonUtil),
                    cacheStorage = FakePhotoStorageSource(jsonUtil, cacheDir = testApplication.cacheDir)))

        ((testApplication.appComponent as TestAppComponent).propertyRepository as DefaultPropertyRepository).remoteDataSource = DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil, cacheDir = testApplication.cacheDir)))

        propertiesRepository = (testApplication.appComponent as TestAppComponent).propertyRepository
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