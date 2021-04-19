package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.graphics.Point
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.*
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.GOOGLE_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INFO_WINDOW_SHOW
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.FakeGlideRequestManager
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import org.hamcrest.core.AllOf.allOf
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(AndroidJUnit4::class)
@MediumTest
class MapFragmentIntegrationTest : BaseMainActivityTests() {

    private lateinit var requestManager: FakeGlideRequestManager
    private lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory
    private lateinit var fakeProperties: List<Property>
    private lateinit var uiDevice: UiDevice
    private lateinit var mapFragment: MapFragment

    private var leChesnay = LatLng(48.82958536116524, 2.125609030745346)

    @Test
    fun when_navigate_in_detail_fragment_then_right_property_is_selected() {
        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        val isTablet = app.resources.getBoolean(R.bool.isTablet)

        if(!isTablet) {
            var masterFragment: BrowseMasterFragment? = null

            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                masterFragment = BrowseMasterFragment()
                it.setFragment(masterFragment!!)
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())
            val mapButton = uiDevice.findObject(UiSelector().textContains(app.resources.getString(R.string.button_map_title)))

            try {
                if(mapButton.exists()) {
                    mapButton.click()

                    val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                            30000)
                    assertThat(mapIsFinishLoading).isTrue()

                    val firstProperty = fakeProperties[0]
                    firstProperty.mainPicture!!.propertyId = firstProperty.id

                    val marker = uiDevice.findObject(UiSelector()
                            .descriptionContains(firstProperty.address!!.street))

                    if(marker.exists()) {
                        marker.click()
                        uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                        val display = mainActivity.windowManager.defaultDisplay
                        val size = Point()
                        display.getRealSize(size)
                        val screenWidth = size.x
                        val screenHeight = size.y
                        val x = screenWidth / 2
                        val y = (screenHeight * 0.43).toInt()

                        // Click on the InfoWindow, using UIAutomator
                        uiDevice.click(x, y)
                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "detail_fragment")), 2000)

                        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

                        val detailFragment: DetailFragment = masterFragment!!.master
                                .childFragmentManager.primaryNavigationFragment as DetailFragment

                        assertThat(detailFragment.property).isEqualTo(firstProperty)

                    }
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }

        if(isTablet) {
            var masterDetailFragment: BrowseMasterDetailFragment? = null

            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                masterDetailFragment = BrowseMasterDetailFragment()
                it.setFragment(masterDetailFragment!!)
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())

            try {
                val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                        30000)
                assertThat(mapIsFinishLoading).isTrue()

                val firstProperty = fakeProperties[0]
                firstProperty.mainPicture!!.propertyId = firstProperty.id

                val marker = uiDevice.findObject(UiSelector()
                        .descriptionContains(firstProperty.address!!.street))

                if(marker.exists()) {
                    marker.click()
                    uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                    val mapFragment = masterDetailFragment!!.detail.childFragmentManager.primaryNavigationFragment as MapFragment
                    val listFragment = masterDetailFragment!!.master.childFragmentManager.primaryNavigationFragment as ListFragment

                    val display = mainActivity.windowManager.defaultDisplay
                    val size = Point()
                    display.getRealSize(size)
                    val screenHeight = size.y
                    val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                    val y = (screenHeight * 0.40).toInt()

                    // Click on the InfoWindow, using UIAutomator
                    uiDevice.click(x, y)
                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "detail_fragment")), 2000)

                    onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

                    val detailFragment: DetailFragment = masterDetailFragment!!.detail
                            .childFragmentManager.primaryNavigationFragment as DetailFragment

                    assertThat(detailFragment.property).isEqualTo(firstProperty)
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun is_navigate_in_detail_fragment_when_click_on_info_window() {

        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        val isTablet = app.resources.getBoolean(R.bool.isTablet)

        if(!isTablet) {
            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                it.setFragment(BrowseMasterFragment())
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())
            val mapButton = uiDevice.findObject(UiSelector().textContains(app.resources.getString(R.string.button_map_title)))

            try {
                if(mapButton.exists()) {
                    mapButton.click()

                    val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                            30000)
                    assertThat(mapIsFinishLoading).isTrue()

                    val firstProperty = fakeProperties[0]

                    val marker = uiDevice.findObject(UiSelector()
                            .descriptionContains(firstProperty.address!!.street))

                    if(marker.exists()) {
                        marker.click()
                        uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                        val display = mainActivity.windowManager.defaultDisplay
                        val size = Point()
                        display.getRealSize(size)
                        val screenWidth = size.x
                        val screenHeight = size.y
                        val x = screenWidth / 2
                        val y = (screenHeight * 0.43).toInt()

                        // Click on the InfoWindow, using UIAutomator
                        uiDevice.click(x, y)
                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "detail_fragment")), 2000)

                        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

                    }
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }

        if(isTablet) {
            var masterDetailFragment: BrowseMasterDetailFragment? = null

            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                masterDetailFragment = BrowseMasterDetailFragment()
                it.setFragment(masterDetailFragment!!)
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())

            try {
                val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                        30000)
                assertThat(mapIsFinishLoading).isTrue()

                val firstProperty = fakeProperties[0]

                val marker = uiDevice.findObject(UiSelector()
                        .descriptionContains(firstProperty.address!!.street))

                if(marker.exists()) {
                    marker.click()
                    uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                    val mapFragment = masterDetailFragment!!.detail.childFragmentManager.primaryNavigationFragment as MapFragment
                    val listFragment = masterDetailFragment!!.master.childFragmentManager.primaryNavigationFragment as ListFragment

                    val display = mainActivity.windowManager.defaultDisplay
                    val size = Point()
                    display.getRealSize(size)
                    val screenHeight = size.y
                    val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                    val y = (screenHeight * 0.40).toInt()

                    // Click on the InfoWindow, using UIAutomator
                    uiDevice.click(x, y)
                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "detail_fragment")), 2000)

                    onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun given_detail_fragment_when_click_on_navigation_tool_bar_then_return_on_map_fragment() {

        val app = getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = ConstantsTest.PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        val isTablet = app.resources.getBoolean(R.bool.isTablet)

        if(!isTablet) {
            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                it.setFragment(BrowseMasterFragment())
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())
            val mapButton = uiDevice.findObject(UiSelector().textContains(app.resources.getString(R.string.button_map_title)))

            try {
                if(mapButton.exists()) {
                    mapButton.click()

                    val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                            30000)
                    assertThat(mapIsFinishLoading).isTrue()

                    val firstProperty = fakeProperties[0]

                    val marker = uiDevice.findObject(UiSelector()
                            .descriptionContains(firstProperty.address!!.street))

                    if(marker.exists()) {
                        marker.click()
                        uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                        val display = mainActivity.windowManager.defaultDisplay
                        val size = Point()
                        display.getRealSize(size)
                        val screenWidth = size.x
                        val screenHeight = size.y
                        val x = screenWidth / 2
                        val y = (screenHeight * 0.43).toInt()

                        // Click on the InfoWindow, using UIAutomator
                        uiDevice.click(x, y)
                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "detail_fragment")), 2000)

                        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

                        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), isDisplayed()))
                                .perform(click())

                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "map_fragment")), 2000)

                        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
                    }
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }

        if(isTablet) {
            var masterDetailFragment: BrowseMasterDetailFragment? = null

            launch(MainActivity::class.java).onActivity {
                INITIAL_ZOOM_LEVEL = 17f
                defaultLocation = leChesnay
                mainActivity = it
                masterDetailFragment = BrowseMasterDetailFragment()
                it.setFragment(masterDetailFragment!!)
            }

            uiDevice = UiDevice.getInstance(getInstrumentation())

            try {
                val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)),
                        30000)
                assertThat(mapIsFinishLoading).isTrue()

                val firstProperty = fakeProperties[0]

                val marker = uiDevice.findObject(UiSelector()
                        .descriptionContains(firstProperty.address!!.street))

                if(marker.exists()) {
                    marker.click()
                    uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

                    val mapFragment = masterDetailFragment!!.detail.childFragmentManager.primaryNavigationFragment as MapFragment
                    val listFragment = masterDetailFragment!!.master.childFragmentManager.primaryNavigationFragment as ListFragment

                    val display = mainActivity.windowManager.defaultDisplay
                    val size = Point()
                    display.getRealSize(size)
                    val screenHeight = size.y
                    val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                    val y = (screenHeight * 0.40).toInt()

                    // Click on the InfoWindow, using UIAutomator
                    uiDevice.click(x, y)
                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "detail_fragment")), 2000)

                    onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

                    onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), isDisplayed()))
                            .perform(click())

                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "map_fragment")), 2000)

                    onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

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

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 17f
            defaultLocation = leChesnay
            MapFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            mapFragment = it
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

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 17f
            defaultLocation = leChesnay
            MapFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            mapFragment = it
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

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        launchFragmentInContainer(null, AppTheme, RESUMED) {
            INITIAL_ZOOM_LEVEL = 17f
            DEFAULT_ZOOM = 17f
            defaultLocation = leChesnay
            MapFragment(propertiesViewModelFactory, requestManager)
        }.onFragment{
            mapFragment = it
        }

        uiDevice = UiDevice.getInstance(getInstrumentation())

        val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)), 15000)
        assertThat(mapIsFinishLoading).isTrue()

        var projection: Projection? = null
        mapFragment.activity!!.runOnUiThread {
            projection = mapFragment.mMap.projection
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
            }
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }

    companion object {
        private val TAG = MapFragmentIntegrationTest::class.simpleName
    }
}

