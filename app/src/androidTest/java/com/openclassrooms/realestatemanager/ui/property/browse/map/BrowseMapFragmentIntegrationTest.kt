package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.android.gms.maps.Projection
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.SMARTPHONE
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.INFO_WINDOW_SHOWN
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.defaultLocation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(AndroidJUnit4::class)
@MediumTest
class BrowseMapFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var browseMapFragment: BrowseMapFragment

    companion object { private val TAG = BrowseMapFragmentIntegrationTest::class.simpleName!! }

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        fakeProperties.forEach { property ->
            property.photos = property.photos.toSet().toMutableList()
        }
        BaseFragment.properties.value = fakeProperties.toMutableList()
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = true
    }

    @After
    public override fun tearDown() {
        if (BaseFragment.properties.value != null) {
            BaseFragment.properties.value!!.clear()
        }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        super.tearDown()
    }

    @Test
    fun given_map_when_navigate_in_detail_then_selected_property_is_shown() {
        // Given Map fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        // When Navigate to Detail fragment
        navigate_to_detail_fragment()

        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

        // Then the detail property is equal to selected property
        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    @Test
    fun given_return_on_map_when_select_an_another_property_then_selected_property_is_shown() {
        // Given Return from Detail to Map fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 15f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        //isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)()

        navigate_to_detail_fragment()
        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])

        wait_until_detail_map_is_finished_loading()

        click_on_navigate_up_button()
        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
            testApplication.resources.getResourceEntryName(R.id.map_fragment))), 10000)

        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))

        // When Select another property
        val marker = uiDevice.findObject(UiSelector()
            .descriptionContains(fakeProperties[itemPosition].address.street))

        if(marker.exists()) { marker.click() }

        var newItemPosition = (fakeProperties.indices).random()
        while (newItemPosition == itemPosition) {
            newItemPosition = (fakeProperties.indices).random()
        }
        itemPosition = newItemPosition

        navigate_to_detail_fragment()
        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

        // Then the detail property is equal to selected property
        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    @Test
    fun given_map_when_click_on_info_window_then_navigate_to_detail() {
        // Given Map fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        // When Click on an info window of property
        navigate_to_detail_fragment()

        // Then Navigate to Detail fragment and fragment is shown
        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_detail_when_click_on_navigation_tool_bar_then_return_on_map() {
        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()
        wait_until_detail_map_is_finished_loading()

        // When click on Navigate Up Home icon
        click_on_navigate_up_button()
        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
            testApplication.resources.getResourceEntryName(R.id.map_fragment))), 10000)

        // Then return on Map fragment and fragment is shown
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_map_when_properties_are_shown_then_properties_are_represented_by_marker() {

        // Given Map fragment
        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 17f
            defaultLocation = leChesnay
            BrowseMapFragment()
        }.onFragment {
            browseMapFragment = it
        }

        // When map is shown
        wait_until_map_is_finished_loading()

        // Then properties are represented by markers on map
        fakeProperties.forEach { property ->
            val marker = uiDevice.findObject(UiSelector()
                .descriptionContains(property.address.street))

            assertThat(marker).isNotNull()
            Timber.tag(TAG).i("/** Marker : ${property.address.street} is not null **/")
        }
    }

    @Test
    fun given_map_when_click_on_a_marker_then_an_info_window_is_shown() {

        // Given Map fragment
        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 15f
            defaultLocation = leChesnay
            BrowseMapFragment()
        }.onFragment {
            browseMapFragment = it
        }
        wait_until_map_is_finished_loading()

        fakeProperties.forEach { property ->
            val marker = uiDevice.findObject(UiSelector()
                .descriptionContains(property.address.street))

            try {
                if(marker.exists()) {

                    // When click on marker
                    marker.click()
                    uiDevice.wait(Until.hasObject(By.text(property.address.street)), 1000)

                    // Then info window is shown
                    val title = uiDevice.findObject(UiSelector().text(property.address.street))
                    assertThat(title).isNotNull()

                    marker.click()
                    onView(withContentDescription(property.address.street)).check(doesNotExist())
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun given_map_when_marker_is_shown_then_marker_position_is_equal_to_property_position() {
        // Given Map fragment
        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 16.5f
            DEFAULT_ZOOM = 17f
            defaultLocation = leChesnay
            BrowseMapFragment()
        }.onFragment{
            browseMapFragment = it
        }
        wait_until_map_is_finished_loading()

        var projection: Projection? = null
        browseMapFragment.activity!!.runOnUiThread {
            projection = browseMapFragment.mMap.projection
        }.let {
            fakeProperties.forEach { property ->
                // When marker is shown
                val marker = uiDevice.findObject(UiSelector().descriptionContains(property.address.street))
                val bounds = marker.bounds

                val position = when(screen_size()) {
                    SMARTPHONE -> { projection!!.fromScreenLocation(
                        Point(bounds.exactCenterX().toInt(), bounds.top))
                    }
                    else -> { projection!!.fromScreenLocation(
                        Point(bounds.exactCenterX().toInt(), bounds.exactCenterY().toInt()))
                    }
                }

                // Then marker position is equal to property data position (latitude and longitude)
                Timber.tag(TAG).i("/** Property: '${property.address.street}'")

                val scale = 3

                Timber.tag(TAG).i("/** markerLatitude: '${position.latitude}'")
                Timber.tag(TAG).i("/** propertyLatitude: '${property.address.latitude}'")

                val markerLatitude = BigDecimal(position.latitude).setScale(scale, RoundingMode.HALF_EVEN)
                val propertyLatitude =  BigDecimal(property.address.latitude).setScale(scale, RoundingMode.CEILING)

                assertThat(markerLatitude).isEqualTo(propertyLatitude)

                Timber.tag(TAG).i(" Marker latitude: $markerLatitude is equal to" +
                        " property latitude: $propertyLatitude with scale of: $scale")

                val markerLongitude = BigDecimal(position.longitude).setScale(scale, RoundingMode.HALF_EVEN)
                val propertyLongitude =  BigDecimal(property.address.longitude).setScale(scale, RoundingMode.HALF_EVEN)

                assertThat(markerLongitude).isEqualTo(propertyLongitude)

                Timber.tag(TAG).i(" Marker longitude: $markerLongitude is equal to" +
                        " property longitude: $propertyLongitude with a scale of: $scale")
            }
        }
    }

    override fun navigate_to_detail_fragment() {
        when(isMasterDetail) {
            true -> navigate_to_detail_fragment_in_master_detail_mode()
            false -> navigate_to_detail_fragment_in_normal_mode()
        }
    }

    override fun navigate_to_detail_fragment_in_normal_mode() {
        try {
            wait_until_map_is_finished_loading()

            val marker = uiDevice.findObject(UiSelector().descriptionContains(
                fakeProperties[itemPosition].address.street))

            if(marker.exists()) {
                marker.click()
                uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOWN)), 50000)

                val displayManager = testApplication.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                val size = Point()
                display.getRealSize(size)
                val screenWidth = size.x
                val screenHeight = size.y
                val x = screenWidth / 2
                val y = (screenHeight * 0.43).toInt()

                // Click on the InfoWindow, using UIAutomator
                uiDevice.click(x, y)
                uiDevice.wait(Until.hasObject(
                    By.res(mainActivity.packageName,
                        mainActivity.resources.getResourceEntryName(R.id.edit_fragment))),
                    30000)
            }
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}

