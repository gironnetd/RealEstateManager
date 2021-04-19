package com.openclassrooms.realestatemanager.ui.property.browse.detail

import android.graphics.Point
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.*
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.ui.property.browse.edit.EditFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.GOOGLE_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INFO_WINDOW_SHOW
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.FakeGlideRequestManager
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.StringContains.containsString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class DetailFragmentIntegrationTest : BaseMainActivityTests() {

    private lateinit var requestManager: FakeGlideRequestManager
    private lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory

    private lateinit var fakeProperties: List<Property>
    private lateinit var uiDevice: UiDevice

    private lateinit var detailFragment: DetailFragment

    private var leChesnay = LatLng(48.82958536116524, 2.125609030745346)

    @Test
    fun check_if_picture_recycler_view_is_not_empty() {

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

        BaseFragment.properties = fakeProperties as MutableList<Property>

        val propertyId = fakeProperties[0].id

        val bundle = bundleOf(FROM to MapFragment::class.java.name,
                PROPERTY_ID to propertyId)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            DetailFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            detailFragment = it
        }

        assertThat(detailFragment.binding.picturesRecyclerView.adapter!!.itemCount).isNotEqualTo(0)
    }

    @Test
    fun check_the_layout_of_the_views_depending_on_whether_it_is_tablet_or_not() {

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

        BaseFragment.properties = fakeProperties as MutableList<Property>

        val propertyId = fakeProperties[0].id

        val bundle = bundleOf(FROM to MapFragment::class.java.name,
                PROPERTY_ID to propertyId)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
           DetailFragment(propertiesViewModelFactory, requestManager)
        }

        onView(withId(R.id.label_media)).check(matches(withText(R.string.media)))

        onView(withId(R.id.pictures_recycler_view)).check(isCompletelyBelow(withId(R.id.label_media)))

        onView(withId(R.id.label_description)).check(isCompletelyBelow(withId(R.id.pictures_recycler_view)))
        onView(withId(R.id.label_description)).check(matches(withText(R.string.description)))

        onView(withId(R.id.layout_surface)).check(isCompletelyAbove(withId(R.id.layout_rooms)))
        onView(withId(R.id.label_surface)).check(matches(withText(R.string.surface)))

        onView(withId(R.id.layout_rooms)).check(isCompletelyAbove(withId(R.id.layout_bathrooms)))
        onView(withId(R.id.label_rooms)).check(matches(withText(R.string.number_of_rooms)))

        onView(withId(R.id.layout_bathrooms)).check(isCompletelyAbove(withId(R.id.layout_bedrooms)))
        onView(withId(R.id.label_bathrooms)).check(matches(withText(R.string.number_of_bathrooms)))

        onView(withId(R.id.layout_bedrooms)).check(isCompletelyBelow(withId(R.id.layout_bathrooms)))
        onView(withId(R.id.label_bedrooms)).check(matches(withText(R.string.number_of_bedrooms)))

        onView(withId(R.id.layout_location)).check(isCompletelyRightOf(allOf(
                withId(R.id.layout_surface))))
        onView(withId(R.id.layout_location)).check(isCompletelyRightOf(allOf(
                withId(R.id.layout_rooms))))
        onView(withId(R.id.layout_location)).check(isCompletelyRightOf(allOf(
                withId(R.id.layout_bathrooms))))
        onView(withId(R.id.layout_location)).check(isCompletelyRightOf(allOf(
                withId(R.id.layout_bedrooms))))

        onView(withId(R.id.label_location)).check(matches(withText(R.string.location)))

        val isTablet = app.resources.getBoolean(R.bool.isTablet)

        if(!isTablet) {
            onView(withId(R.id.map_fragment)).check(isCompletelyBelow(withId(R.id.layout_bathrooms)))
        }

        if(isTablet) {
            onView(withId(R.id.map_fragment)).check(isCompletelyRightOf(withId(R.id.layout_location)))
        }
    }

    @Test
    fun check_if_data_is_successfully_displayed_in_detail_fragment() {

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

        BaseFragment.properties = fakeProperties as MutableList<Property>

        val propertyId = fakeProperties[0].id

        val bundle = bundleOf(FROM to MapFragment::class.java.name,
                PROPERTY_ID to propertyId)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            DetailFragment(propertiesViewModelFactory, requestManager)
        }

        onView(withId(R.id.text_description)).check(matches(withText(fakeProperties[0].description)))

        onView(withId(R.id.surface)).check(matches(withText(containsString(fakeProperties[0].surface.toString()))))

        onView(withId(R.id.rooms)).check(matches(withText(fakeProperties[0].rooms.toString())))
        onView(withId(R.id.bathrooms)).check(matches(withText(fakeProperties[0].bathRooms.toString())))
        onView(withId(R.id.bedrooms)).check(matches(withText(fakeProperties[0].bedRooms.toString())))
        onView(withId(R.id.location)).check(matches(withText(fakeProperties[0].address.toString())))

    }

    @Test
    fun when_navigate_in_edit_fragment_then_right_property_is_selected() {
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

                        onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                                .perform(ViewActions.click())

                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "edit_fragment")), 2000)

                        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

                        val editFragment: EditFragment = masterFragment!!.master
                                .childFragmentManager.primaryNavigationFragment as EditFragment

                        assertThat(editFragment.property).isEqualTo(firstProperty)
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

                    onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                            .perform(ViewActions.click())

                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "edit_fragment")), 2000)

                    onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

                    val editFragment: EditFragment = masterDetailFragment!!
                            .detail.childFragmentManager.primaryNavigationFragment as EditFragment

                    assertThat(editFragment.property).isEqualTo(firstProperty)
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun is_navigate_in_edit_fragment_when_click_on_menu_item() {

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

                        onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                                .perform(ViewActions.click())

                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "edit_fragment")), 2000)

                        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
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

                    onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                            .perform(ViewActions.click())

                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "edit_fragment")), 2000)

                    onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }


    @Test
    fun given_edit_fragment_when_click_on_navigation_tool_bar_then_return_on_detail_fragment() {

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

                        onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                                .perform(ViewActions.click())

                        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                                "edit_fragment")), 2000)

                        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

                        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), isDisplayed()))
                                .perform(ViewActions.click())

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

                    onView(allOf(withId(R.id.navigation_edit), isDisplayed()))
                            .perform(ViewActions.click())

                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "edit_fragment")), 2000)

                    onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

                    onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), isDisplayed()))
                            .perform(ViewActions.click())

                    uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                            "detail_fragment")), 2000)

                    onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
                }
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }


    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}