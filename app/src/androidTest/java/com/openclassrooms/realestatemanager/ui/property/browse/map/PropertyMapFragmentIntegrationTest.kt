package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.graphics.Point
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.*
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.property.browse.map.PropertyMapFragment.Companion.GOOGLE_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyMapFragmentIntegrationTest : BaseMainActivityTests() {

    @get: Rule val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory
    lateinit var fakeProperties: List<Property>
    lateinit var uiDevice: UiDevice
    lateinit var propertyMapFragment: PropertyMapFragment

    @Test
    fun verify_if_each_property_is_displayed_on_map_by_marker() {
        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyMapFragment(propertiesViewModelFactory)
        }.onFragment {
            propertyMapFragment = it
        }

        uiDevice = UiDevice.getInstance(getInstrumentation())

        val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)), 15000)
        assertThat(mapIsFinishLoading).isTrue()

        fakeProperties.forEach { property ->
            val marker = uiDevice.findObject(UiSelector()
                    .descriptionContains(property.address!!.street))

            assertThat(marker).isNotNull()
            Timber.tag(TAG).i("/** Marker : ${property.address!!.street} is not null **/")
        }
    }

    @Test
    fun verify_if_title_is_not_null_when_click_on_marker() {
        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyMapFragment(propertiesViewModelFactory)
        }

        uiDevice = UiDevice.getInstance(getInstrumentation())

        val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)), 15000)
        assertThat(mapIsFinishLoading).isTrue()

        fakeProperties.forEach { property ->
            val marker = uiDevice.findObject(UiSelector()
                    .descriptionContains(property.address!!.street))

            try {
                if(marker.exists()) {
                    marker.click()
                    uiDevice.wait(Until.hasObject(By.text(property.address!!.street)), 1000);

                    val title = uiDevice.findObject(UiSelector().text(property.address!!.street))
                    assertThat(title).isNotNull()

                    marker.click()
                    onView(withContentDescription(property.address!!.street)).check(doesNotExist())
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun verify_if_marker_position_on_map_is_equal_to_property_coordinates() {
        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyMapFragment(propertiesViewModelFactory)
        }.onFragment{
            propertyMapFragment = it
        }

        uiDevice = UiDevice.getInstance(getInstrumentation())

        val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)), 15000)
        assertThat(mapIsFinishLoading).isTrue()

        var projection: Projection? = null
        propertyMapFragment.activity!!.runOnUiThread {
            projection = propertyMapFragment.mMap.projection
        }.let {
            fakeProperties.forEach { property ->
                val marker = uiDevice.findObject(UiSelector()
                        .descriptionContains(property.address!!.street))

                val bounds = marker.visibleBounds

                val position: LatLng = projection!!.fromScreenLocation(
                        Point(bounds.exactCenterX().toInt(),
                                bounds.exactCenterY().toInt()
                        ))

                Timber.tag(TAG).i("/** Property: '${property.address!!.street}'")

                val scale = 4

                val markerLatitude = BigDecimal(position.latitude).setScale(scale, RoundingMode.HALF_EVEN)
                val propertyLatitude =  BigDecimal(property.address!!.latitude).setScale(scale, RoundingMode.HALF_EVEN)

                assertThat(markerLatitude).isEqualTo(propertyLatitude)

                Timber.tag(TAG).i(" Marker latitude: $markerLatitude is equal to" +
                        " property latitude: $propertyLatitude with scale of: $scale")

                val markerLongitude = BigDecimal(position.longitude).setScale(scale, RoundingMode.HALF_EVEN)
                val propertyLongitude =  BigDecimal(property.address!!.longitude).setScale(scale, RoundingMode.HALF_EVEN)

                assertThat(markerLongitude).isEqualTo(propertyLongitude)

                Timber.tag(TAG).i(" Marker longitude: $markerLongitude is equal to" +
                        " property longitude: $propertyLongitude with a scale of: $scale")

                val i = ""
            }
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }

    companion object {
        private val TAG = PropertyMapFragmentIntegrationTest::class.simpleName
    }
}

