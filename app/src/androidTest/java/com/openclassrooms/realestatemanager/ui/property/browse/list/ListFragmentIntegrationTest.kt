package com.openclassrooms.realestatemanager.ui.property.browse.list

import android.graphics.Point
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListAdapter.PropertyViewHolder
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.GOOGLE_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INFO_WINDOW_SHOW
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import org.hamcrest.core.AllOf
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ListFragmentIntegrationTest : BaseMainActivityTests() {

    @get: Rule val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    lateinit var apiService: FakePropertyApiService
    private lateinit var fakeProperties: List<Property>

    private lateinit var uiDevice: UiDevice
    private var leChesnay = LatLng(48.82958536116524, 2.125609030745346)

    companion object {
        const val LIST_ITEM_SELECTED = 2
    }

    val app = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext as TestBaseApplication

    @Before
    public override fun setUp() {
        super.setUp()
        apiService = configureFakeApiService(
                propertiesDataSource = PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )
        BrowseFragment.NAV_HOST_FRAGMENT_SELECTED_WHEN_NON_TABLET_MODE = ListFragment::class.java.name
    }

    @Test
    fun is_property_list_empty() {
        BaseFragment.properties.clear()
        apiService.propertiesJsonFileName = EMPTY_LIST

        configureFakeRepository(apiService, app)
        injectTest(app)

        launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }

        val recyclerView = onView(withId(R.id.recycler_view))

        recyclerView.check(matches(withEffectiveVisibility(GONE)))

        onView(withId(R.id.no_data_text_view))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun is_properties_list_scrolling() {

        apiService.propertiesJsonFileName = PROPERTIES_DATA_FILENAME

        configureFakeRepository(apiService, app)
        injectTest(app)

        launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }.onFragment {
            val params = it.binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = 0
            it.binding.recyclerView.requestLayout()
            it.binding.recyclerView.adapter!!.notifyDataSetChanged()
        }

        val recyclerView = onView(withId(R.id.recycler_view))

        recyclerView.check(matches(isDisplayed()))

        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<PropertyViewHolder>(2)
        )
        onView(withText("2-8 Square de Castiglione")).check(matches(isDisplayed()))

        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8))

        onView(withText("3 Place de la Loi")).check(matches(isDisplayed()))

        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<PropertyViewHolder>(0))

        onView(withText("3 Square Fantin Latour")).check(matches(isDisplayed()))

        onView(withId(R.id.no_data_text_view))
            .check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun is_instance_state_saved_and_restored_on_destroy_activity() {

        apiService.propertiesJsonFileName = PROPERTIES_DATA_FILENAME

        configureFakeRepository(apiService, app)
        injectTest(app)

        val scenario = launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }.onFragment {
            val params = it.binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = 0
            it.binding.recyclerView.requestLayout()
            it.binding.recyclerView.adapter!!.notifyDataSetChanged()
        }

        val recyclerView = onView(withId(R.id.recycler_view))

        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))

        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8)
        )
        onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))

        scenario.recreate()

        recyclerView.perform(
            RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8)
        )
        onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigate_in_detail_fragment_then_right_property_is_selected() {

        val apiService = configureFakeApiService(
                propertiesDataSource = PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        val firstProperty = fakeProperties[LIST_ITEM_SELECTED]
        firstProperty.mainPicture!!.propertyId = firstProperty.id

        var browseFragment: BrowseFragment ? = null

        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment!!)
        }

        val isMasterDetail = app.resources.getBoolean(R.bool.isMasterDetail)

        if(!isMasterDetail) {
            navigateToDetailFragmentInNormalMode()
        }

        if(isMasterDetail) {
            navigateToDetailFragmentInMasterDetailMode(browseFragment = browseFragment!!)
        }

        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

        val detailFragment: DetailFragment = browseFragment!!.detail
            .childFragmentManager.primaryNavigationFragment as DetailFragment

        assertThat(detailFragment.property).isEqualTo(firstProperty)
    }

    @Test
    fun given_detail_fragment_when_click_on_navigation_tool_bar_then_return_on_list_fragment() {

        val apiService = configureFakeApiService(
                propertiesDataSource = PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        val isMasterDetail = app.resources.getBoolean(R.bool.isMasterDetail)

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            it.setFragment(BrowseFragment())
        }

        if(!isMasterDetail) {

            navigateToDetailFragmentInNormalMode()

            onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

            onView(AllOf.allOf(withContentDescription(R.string.abc_action_bar_up_description), isDisplayed()))
                    .perform(click())

            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                    "list_fragment")), 2000)

            onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))
        }
    }


    @Test
    fun is_navigate_in_detail_fragment_when_click_on_item() {

        val apiService = configureFakeApiService(
                propertiesDataSource = PROPERTIES_DATA_FILENAME, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()

        var browseFragment: BrowseFragment ? = null

        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment!!)
        }

        val isMasterDetail = app.resources.getBoolean(R.bool.isMasterDetail)

        if(!isMasterDetail) {

            val recyclerView = onView(withId(R.id.recycler_view))
            recyclerView.check(matches(isDisplayed()))

            recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<PropertyViewHolder>(
                LIST_ITEM_SELECTED, click()))
            onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
        }

        if(isMasterDetail) {
            navigateToDetailFragmentInMasterDetailMode(browseFragment = browseFragment!!)
            try {
                onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
            } catch (e: UiObjectNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateToDetailFragmentInNormalMode() {
        val recyclerView = onView(withId(R.id.recycler_view))
        recyclerView.check(matches(isDisplayed()))

        recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<PropertyViewHolder>(
            LIST_ITEM_SELECTED,
            click()))
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
    }

    private fun navigateToDetailFragmentInMasterDetailMode(browseFragment: BrowseFragment) {

        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        try {

            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            val mapIsFinishLoading = uiDevice.wait(Until.hasObject(By.desc(GOOGLE_MAP_FINISH_LOADING)), 20000)
            assertThat(mapIsFinishLoading).isTrue()

            val firstProperty = fakeProperties[LIST_ITEM_SELECTED]

            val recyclerView = onView(withId(R.id.recycler_view))
            recyclerView.check(matches(isDisplayed()))

            recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<PropertyViewHolder>(
                LIST_ITEM_SELECTED,
                click()))

            uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 15000)

            val title = uiDevice.findObject(UiSelector().text(firstProperty.address!!.street))

            if(title.exists()) {
                uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOW)), 30000)

                val mapFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as MapFragment
                val listFragment = browseFragment.master

                val display = mainActivity.windowManager.defaultDisplay
                val size = Point()
                display.getRealSize(size)
                val screenHeight = size.y
                val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                val y = (screenHeight * 0.40).toInt()

                // Click on the InfoWindow, using UIAutomator
                uiDevice.click(x, y)
                uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                    "detail_fragment")), 10000)
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