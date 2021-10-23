package com.openclassrooms.realestatemanager.ui.property.propertydetail

import android.content.res.Configuration
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.view.size
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.Utils
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyDetailFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var propertyDetailFragment: PropertyDetailFragment

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        BaseFragment.properties.value = fakeProperties.toMutableList()
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        if(BaseFragment.properties.value != null) { BaseFragment.properties.value!!.clear() }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        super.tearDown()
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_adapter_is_not_null() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        scenario.onActivity {
            propertyDetailFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
        }

        // Then Photos recyclerview adapter is not null
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter).isNotNull()
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_is_not_empty() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        scenario.onActivity {
            propertyDetailFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
        }
        // Then Photos recyclerview adapter is not empty
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter!!.itemCount).isNotEqualTo(0)
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_count_is_equal_to_property_photo_count() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        scenario.onActivity {
            propertyDetailFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
        }

        // Then Photos recyclerview item count is equal to selected property photos number
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter!!.itemCount)
            .isEqualTo(fakeProperties[itemPosition].photos.size)
    }

    @Test
    fun given_detail_when_is_shown_then_the_layout_of_the_views_depending_on_whether_it_is_tablet_or_not() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        // Then the layout of views is correct depending on configuration Master/ Detail or Not
        onView(withId(R.id.label_media)).check(matches(withText(R.string.media)))

        onView(withId(R.id.photos_recycler_view)).check(isPartiallyBelow(withId(R.id.label_media)))

        onView(withId(R.id.description_text_input_layout)).check(isCompletelyBelow(withId(R.id.photos_recycler_view)))

        onView(withId(R.id.entry_date_text_input_layout)).check(isCompletelyBelow(withId(R.id.description_text_input_layout)))
        onView(withId(R.id.status_text_input_layout)).check(isCompletelyBelow(withId(R.id.description_text_input_layout)))

        onView(withId(R.id.interest_points_chip_group)).check(isCompletelyBelow(withId(R.id.entry_date_text_input_layout)))
        onView(withId(R.id.interest_points_chip_group)).check(isCompletelyBelow(withId(R.id.status_text_input_layout)))

        if (testApplication.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            Configuration.SCREENLAYOUT_SIZE_NORMAL
        ) {
            onView(withId(R.id.price_text_input_layout)).check(isCompletelyBelow(withId(R.id.interest_points_chip_group)))
            onView(withId(R.id.price_text_input_layout)).check(isCompletelyAbove(withId(R.id.type_text_input_layout)))

            onView(withId(R.id.type_text_input_layout)).check(isCompletelyBelow(withId(R.id.price_text_input_layout)))
            onView(withId(R.id.type_text_input_layout)).check(isCompletelyAbove(withId(R.id.surface_text_input_layout)))

            onView(withId(R.id.surface_text_input_layout)).check(isCompletelyBelow(withId(R.id.type_text_input_layout)))
            onView(withId(R.id.surface_text_input_layout)).check(isCompletelyAbove(withId(R.id.rooms_text_input_layout)))

            onView(withId(R.id.rooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.surface_text_input_layout)))
            onView(withId(R.id.rooms_text_input_layout)).check(isCompletelyAbove(withId(R.id.bathrooms_text_input_layout)))

            onView(withId(R.id.bathrooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.rooms_text_input_layout)))
            onView(withId(R.id.bathrooms_text_input_layout)).check(isCompletelyAbove(withId(R.id.bedrooms_text_input_layout)))

            onView(withId(R.id.bedrooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.bathrooms_text_input_layout)))
        }

        if (testApplication.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            (Configuration.SCREENLAYOUT_SIZE_LARGE or Configuration.SCREENLAYOUT_SIZE_XLARGE)
        ) {
            onView(withId(R.id.price_text_input_layout)).check(isCompletelyBelow(withId(R.id.interest_points_chip_group)))
            onView(withId(R.id.price_text_input_layout)).check(isCompletelyLeftOf(withId(R.id.type_text_input_layout)))

            onView(withId(R.id.type_text_input_layout)).check(isCompletelyBelow(withId(R.id.interest_points_chip_group)))
            onView(withId(R.id.type_text_input_layout)).check(isCompletelyRightOf(withId(R.id.price_text_input_layout)))

            onView(withId(R.id.surface_text_input_layout)).check(isCompletelyBelow(withId(R.id.price_text_input_layout)))
            onView(withId(R.id.surface_text_input_layout)).check(isCompletelyLeftOf(withId(R.id.rooms_text_input_layout)))

            onView(withId(R.id.rooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.type_text_input_layout)))
            onView(withId(R.id.rooms_text_input_layout)).check(isCompletelyRightOf(withId(R.id.surface_text_input_layout)))

            onView(withId(R.id.bathrooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.surface_text_input_layout)))
            onView(withId(R.id.bathrooms_text_input_layout)).check(isCompletelyLeftOf(withId(R.id.bedrooms_text_input_layout)))

            onView(withId(R.id.bedrooms_text_input_layout)).check(isCompletelyBelow(withId(R.id.rooms_text_input_layout)))
            onView(withId(R.id.bedrooms_text_input_layout)).check(isCompletelyRightOf(withId(R.id.bathrooms_text_input_layout)))
        }

        onView(withId(R.id.location_layout))
            .check(isCompletelyBelow(withId(R.id.bathrooms_text_input_layout)))
            .check(isCompletelyBelow(withId(R.id.bedrooms_text_input_layout)))

        if (testApplication.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            Configuration.SCREENLAYOUT_SIZE_NORMAL
        ) {
            onView(withId(R.id.street_text_input_layout)).check(isCompletelyAbove(withId(R.id.city_text_input_layout)))
            onView(withId(R.id.city_text_input_layout)).check(isCompletelyAbove(withId(R.id.postal_code_text_input_layout)))
            onView(withId(R.id.postal_code_text_input_layout)).check(isCompletelyBelow(withId(R.id.city_text_input_layout)))
            onView(withId(R.id.country_text_input_layout)).check(isCompletelyBelow(withId(R.id.postal_code_text_input_layout)))
            onView(withId(R.id.state_text_input_layout)).check(isCompletelyBelow(withId(R.id.country_text_input_layout)))
        }

        if (testApplication.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            (Configuration.SCREENLAYOUT_SIZE_LARGE or Configuration.SCREENLAYOUT_SIZE_XLARGE)
        ) {
            onView(withId(R.id.street_text_input_layout))
                .check(isCompletelyAbove(withId(R.id.city_text_input_layout)))
                .check(isCompletelyAbove(withId(R.id.postal_code_text_input_layout)))

            onView(withId(R.id.city_text_input_layout)).check(isCompletelyLeftOf(withId(R.id.postal_code_text_input_layout)))
            onView(withId(R.id.postal_code_text_input_layout)).check(isCompletelyRightOf(withId(R.id.city_text_input_layout)))

            onView(withId(R.id.country_text_input_layout))
                .check(isCompletelyBelow(withId(R.id.city_text_input_layout)))
                .check(isCompletelyLeftOf(withId(R.id.state_text_input_layout)))
                .check(isCompletelyLeftOf(withId(R.id.postal_code_text_input_layout)))

            onView(withId(R.id.state_text_input_layout))
                .check(isCompletelyBelow(withId(R.id.postal_code_text_input_layout)))
                .check(isCompletelyRightOf(withId(R.id.country_text_input_layout)))
                .check(isCompletelyRightOf(withId(R.id.city_text_input_layout)))
        }
    }

    @Test
    fun given_detail_when_is_shown_then_data_is_successfully_displayed_in_detail_fragment() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        scenario.onActivity {
            propertyDetailFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
        }

        // Then verify that the data are correctly displayed
        make_sure_that_content_view_is_equal_to_detail_property_value()
    }

    @Test
    fun given_detail_when_switching_between_properties_then_content_view_are_correct() {
        // Given Detail fragment
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        scenario.onActivity {
            propertyDetailFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
        }

        make_sure_that_content_view_is_equal_to_detail_property_value()
        click_on_navigate_up_button()

        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
            testApplication.resources.getResourceEntryName(R.id.properties_recycler_view))), 10000)

        if(isMasterDetail) {
            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                testApplication.resources.getResourceEntryName(R.id.map_fragment))), 10000)
            onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))

            val marker = uiDevice.findObject(UiSelector()
                .descriptionContains(fakeProperties[itemPosition].address.street))
            if(marker.exists()) { marker.click() }
        }

        // When Select another property
        var newItemPosition = (fakeProperties.indices).random()
        while (newItemPosition == itemPosition) {
            newItemPosition = (fakeProperties.indices).random()
        }
        itemPosition = newItemPosition
        navigate_to_detail_fragment()

        // Then verify that the data of new selected property are correctly displayed
        make_sure_that_content_view_is_equal_to_detail_property_value()
    }

    @Test
    fun given_detail_when_navigate_in_edit_fragment_then_right_property_is_selected() {
        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        // When Navigate to Edit fragment
        navigate_to_update_fragment()

        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))

        // Then the edit property is equal to selected property
        assertThat(obtainUpdateFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    @Test
    fun given_detail_when_click_on_menu_item_then_is_navigate_to_edit() {
        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        // When Click on update menu item
        navigate_to_update_fragment()

        // Then Navigate to Edit fragment and fragment is shown
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_edit_when_click_on_navigation_tool_bar_then_return_on_detail_fragment() {
        // Given Edit fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        try {
            navigate_to_update_fragment()
            onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))

            // When click on Navigate Up Home icon
            click_on_navigate_up_button()

            // Then return on Detail fragment and fragment is shown
            onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun make_sure_that_content_view_is_equal_to_detail_property_value() {

        onView(withId(R.id.description)).check(matches(withText(containsString(fakeProperties[itemPosition].description))))
        onView(withId(R.id.entry_date)).check(matches(withText(Utils.formatDate(fakeProperties[itemPosition].entryDate))))

        if(fakeProperties[itemPosition].soldDate != null) {
            onView(withId(R.id.sold_date)).check(matches(
                withText(Utils.formatDate(fakeProperties[itemPosition].soldDate) ?:
                testApplication.resources.getString(fakeProperties[itemPosition].status.status))))
        } else {
            onView(withId(R.id.sold_date)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }

        onView(withId(R.id.interest_points_chip_group)).perform(scrollTo())

        assertThat(propertyDetailFragment.binding.interestPointsChipGroup.size)
            .isEqualTo(fakeProperties[itemPosition].interestPoints.size)

        fakeProperties[itemPosition].interestPoints.forEach { interestPoint ->
            onView(withText(interestPoint.place)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.price)).check(matches(withText(containsString(fakeProperties[itemPosition].price.toString()))))
        onView(withId(R.id.type)).check(matches(withText(fakeProperties[itemPosition].type.type)))

        onView(withId(R.id.surface)).check(matches(withText(containsString(fakeProperties[itemPosition].surface.toString()))))
        onView(withId(R.id.rooms)).check(matches(withText(fakeProperties[itemPosition].rooms.toString())))
        onView(withId(R.id.bathrooms)).check(matches(withText(fakeProperties[itemPosition].bathRooms.toString())))
        onView(withId(R.id.bedrooms)).check(matches(withText(fakeProperties[itemPosition].bedRooms.toString())))

        onView(withId(R.id.street)).check(matches(withText(fakeProperties[itemPosition].address.street)))
        onView(withId(R.id.city)).check(matches(withText(fakeProperties[itemPosition].address.city)))
        onView(withId(R.id.postal_code)).check(matches(withText(fakeProperties[itemPosition].address.postalCode)))
        onView(withId(R.id.country)).check(matches(withText(fakeProperties[itemPosition].address.country)))
        onView(withId(R.id.state)).check(matches(withText(fakeProperties[itemPosition].address.state)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}