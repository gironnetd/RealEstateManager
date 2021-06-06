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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListAdapter.PropertyViewHolder
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INFO_WINDOW_SHOWN
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ListFragmentIntegrationTest : BaseFragmentTests() {

    @get: Rule val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository(apiService = configure_fake_api_service(
            propertiesDataSource = PROPERTIES_DATA_FILENAME,
            networkDelay = 0L)
        )
        injectTest(testApplication)

        fakeProperties = propertiesRepository.apiService.findAllProperties().blockingGet()
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @Test
    fun given_list_when_properties_are_empty_then_property_list_empty() {

        // Given List fragment
        BaseFragment.properties.clear()

        // When properties list is empty
        apiService.propertiesJsonFileName = EMPTY_LIST

        launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }

        // Then recyclerview is not shown and message is displayed
        onView(withId(R.id.no_data_text_view)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withId(R.id.recycler_view)).check(matches(withEffectiveVisibility(GONE)))
    }

    @Test
    fun given_list_when_properties_are_not_empty_then_properties_list_scrolling() {

        // Given List fragment

        // When properties list is not empty
        launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }

        // Then verify recyclerview is displayed and list scroll
        with(onView(withId(R.id.recycler_view))) {
            onView(withId(R.id.no_data_text_view)).check(matches(withEffectiveVisibility(GONE)))
            check(matches(isDisplayed()))

            perform(RecyclerViewActions.scrollToPosition<PropertyViewHolder>(2))
            onView(withText(fakeProperties[2].address!!.street)).check(matches(isDisplayed()))

            perform(RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8))
            onView(withText(fakeProperties[8].address!!.street)).check(matches(isDisplayed()))

            perform(RecyclerViewActions.scrollToPosition<PropertyViewHolder>(0))
            onView(withText(fakeProperties[0].address!!.street)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_list_when_destroy_activity_then_instance_state_saved_and_restored() {

        // Given List fragment
        val scenario = launchFragmentInContainer(null, AppTheme) {
            ListFragment()
        }.onFragment {
            val params = it.binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = 0
            it.binding.recyclerView.requestLayout()
            it.binding.recyclerView.adapter!!.notifyDataSetChanged()
        }

        with(onView(withId(R.id.recycler_view))) {
            check(matches(isDisplayed()))
            perform(RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8))
            onView(withText(fakeProperties[8].address!!.street)).check(matches(isDisplayed()))

            // When activity is destroyed
            scenario.recreate()

            // Then the instance is saved and restored
            perform(RecyclerViewActions.scrollToPosition<PropertyViewHolder>(8))
            onView(withText(fakeProperties[8].address!!.street)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_list_when_navigate_to_detail_then_selected_property_is_shown() {

        // Given List fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        // When Navigate to Detail fragment
        navigate_to_detail_fragment()

        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

        // Then the detail property is equal to selected property
        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    @Test
    fun given_list_when_click_on_item_then_navigate_to_detail() {

        // Given List fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        // When Click on item
        navigate_to_detail_fragment()

        // Then Navigate to Detail fragment and fragment is shown
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_detail_when_click_on_navigation_tool_bar_then_return_on_list() {

        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

        // When click on Navigate Up Home icon
        click_on_navigate_up_button()

        if(!isMasterDetail) {
            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                testApplication.resources.getResourceEntryName(R.id.list_fragment))), 2000)

            // Then return on List fragment and fragment is shown
            onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))
        }

        if(isMasterDetail) {
            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                testApplication.resources.getResourceEntryName(R.id.map_fragment))), 2000)

            // Then return on Map fragment and fragment is shown
            onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_return_on_list_when_select_an_another_property_then_selected_property_is_shown() {

        // Given Return from Detail to List fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])

        if(!isMasterDetail) { click_on_navigate_up_button() }

        // When Select another property
        var newItemPosition = (fakeProperties.indices).random()
        while (newItemPosition == itemPosition) {
            newItemPosition = (fakeProperties.indices).random()
        }
        itemPosition = newItemPosition

        if(!isMasterDetail) { navigate_to_detail_fragment_in_normal_mode() }

        if(isMasterDetail) {
            with(onView(withId(R.id.recycler_view))) {
                check(matches(isDisplayed()))
                perform(RecyclerViewActions.actionOnItemAtPosition<PropertyViewHolder>(
                    itemPosition, click()))
            }
        }

        // Then the detail property is equal to new selected property
        assertThat(obtainDetailFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    override fun navigate_to_detail_fragment_in_master_detail_mode() {
        try {
            wait_until_map_is_finished_loading()

            with(onView(withId(R.id.recycler_view))) {
                check(matches(isDisplayed()))
                perform(RecyclerViewActions.actionOnItemAtPosition<PropertyViewHolder>(itemPosition,
                    click()))
            }
            uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOWN)), 15000)

            if(uiDevice.findObject(UiSelector().text(fakeProperties[itemPosition].address!!.street)).exists()) {
                uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOWN)), 30000)

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
                    testApplication.resources.getResourceEntryName(R.id.detail_fragment))), 10000)
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