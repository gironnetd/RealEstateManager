package com.openclassrooms.realestatemanager.ui.property.propertydetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.core.view.size
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
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
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.Utils
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

        fakeProperties = propertiesRepository.findAllProperties().blockingFirst().right!!
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        BaseFragment.properties.value!!.clear()
        super.tearDown()
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_adapter_is_not_null() {

        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        // When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        // Then Photos recyclerview adapter is not null
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter).isNotNull()
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_is_not_empty() {

        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        // When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        // Then Photos recyclerview adapter is not empty
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter!!.itemCount).isNotEqualTo(0)
    }

    @Test
    fun given_detail_when_is_shown_then_photo_recycler_view_count_is_equal_to_property_photo_count() {

        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        // When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        // Then Photos recyclerview item count is equal to selected property photos number
        assertThat(propertyDetailFragment.binding.photosRecyclerView.adapter!!.itemCount)
            .isEqualTo(fakeProperties[itemPosition].photos.size)
    }

    @Test
    fun given_detail_when_is_shown_then_the_layout_of_the_views_depending_on_whether_it_is_tablet_or_not() {

        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        // When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }

        // Then the layout of views is correct depending on configuration Master/ Detail or Not
        onView(withId(R.id.label_media)).check(matches(withText(R.string.media)))

        onView(withId(R.id.photos_recycler_view)).check(isCompletelyBelow(withId(R.id.label_media)))

        onView(withId(R.id.description_text_input_layout)).check(isCompletelyBelow(withId(R.id.layout_media)))

        onView(withId(R.id.entry_date_text_input_layout)).check(isCompletelyBelow(withId(R.id.description_text_input_layout)))
        onView(withId(R.id.status_text_input_layout)).check(isCompletelyBelow(withId(R.id.description_text_input_layout)))

        onView(withId(R.id.layout_interest_points)).check(isCompletelyBelow(withId(R.id.entry_date_text_input_layout)))
        onView(withId(R.id.layout_interest_points)).check(isCompletelyBelow(withId(R.id.status_text_input_layout)))

        onView(withId(R.id.layout_price)).check(isCompletelyBelow(withId(R.id.layout_interest_points)))
        onView(withId(R.id.layout_price)).check(isCompletelyLeftOf(withId(R.id.layout_type)))

        onView(withId(R.id.layout_type)).check(isCompletelyBelow(withId(R.id.layout_interest_points)))
        onView(withId(R.id.layout_type)).check(isCompletelyRightOf(withId(R.id.layout_price)))

        onView(withId(R.id.layout_surface)).check(isCompletelyBelow(withId(R.id.layout_price)))
        onView(withId(R.id.layout_surface)).check(isCompletelyLeftOf(withId(R.id.layout_rooms)))

        onView(withId(R.id.layout_rooms)).check(isCompletelyBelow(withId(R.id.layout_type)))
        onView(withId(R.id.layout_rooms)).check(isCompletelyRightOf(withId(R.id.layout_surface)))

        onView(withId(R.id.layout_bathrooms)).check(isCompletelyBelow(withId(R.id.layout_surface)))
        onView(withId(R.id.layout_bathrooms)).check(isCompletelyLeftOf(withId(R.id.layout_bedrooms)))

        onView(withId(R.id.layout_bedrooms)).check(isCompletelyBelow(withId(R.id.layout_rooms)))
        onView(withId(R.id.layout_bedrooms)).check(isCompletelyRightOf(withId(R.id.layout_bathrooms)))

        onView(withId(R.id.layout_location))
            .check(isCompletelyBelow(withId(R.id.layout_bathrooms)))
            .check(isCompletelyBelow(withId(R.id.layout_bedrooms)))


        when(isMasterDetail) {
            true -> {
                onView(withId(R.id.layout_property_address)).check(isCompletelyLeftOf(withId(R.id.map_constraint_layout)))
                onView(withId(R.id.map_constraint_layout)).check(isCompletelyRightOf(withId(R.id.layout_property_address)))

                onView(withId(R.id.street_text_input_layout)).check(isCompletelyAbove(withId(R.id.city_text_input_layout)))
                onView(withId(R.id.city_text_input_layout)).check(isCompletelyBelow(withId(R.id.street_text_input_layout)))

                onView(withId(R.id.city_text_input_layout)).check(isCompletelyAbove(withId(R.id.postal_code_text_input_layout)))
                onView(withId(R.id.postal_code_text_input_layout)).check(isCompletelyBelow(withId(R.id.city_text_input_layout)))

                onView(withId(R.id.postal_code_text_input_layout)).check(isCompletelyAbove(withId(R.id.country_text_input_layout)))
                onView(withId(R.id.country_text_input_layout)).check(isCompletelyBelow(withId(R.id.postal_code_text_input_layout)))

                onView(withId(R.id.country_text_input_layout)).check(isCompletelyAbove(withId(R.id.state_text_input_layout)))
                onView(withId(R.id.state_text_input_layout)).check(isCompletelyBelow(withId(R.id.country_text_input_layout)))
            }
            false -> {
                onView(withId(R.id.layout_property_address)).check(isCompletelyAbove(withId(R.id.map_constraint_layout)))
                onView(withId(R.id.map_constraint_layout)).check(isCompletelyBelow(withId(R.id.layout_property_address)))

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
    }

    @Test
    fun given_detail_when_is_shown_then_data_is_successfully_displayed_in_detail_fragment() {

        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to MapFragment::class.java.name,
            PROPERTY_ID to fakeProperties[itemPosition].id)

        // When fragment is launched
        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyDetailFragment(propertiesViewModelFactory/*, requestManager*/)
        }.onFragment {
            propertyDetailFragment = it
        }

        // Then verify that the data are correctly displayed
        make_sure_that_content_view_is_equal_to_detail_property_value()
    }

    @Test
    fun given_detail_when_switching_between_properties_then_content_view_are_correct() {

        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()
        propertyDetailFragment = obtainDetailFragment()

        make_sure_that_content_view_is_equal_to_detail_property_value()
        click_on_navigate_up_button()

        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
            testApplication.resources.getResourceEntryName(R.id.list_fragment))), 10000)

        if(isMasterDetail) {
            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                testApplication.resources.getResourceEntryName(R.id.map_fragment))), 10000)
            onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))

            val marker = uiDevice.findObject(UiSelector()
                .descriptionContains(fakeProperties[itemPosition].address!!.street))
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
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        // When Navigate to Edit fragment
        navigate_to_update_fragment()

        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

        // Then the edit property is equal to selected property
        assertThat(obtainUpdateFragment().property).isEqualTo(fakeProperties[itemPosition])
    }

    @Test
    fun given_detail_when_click_on_menu_item_then_is_navigate_to_edit() {

        // Given Detail fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        // When Click on update menu item
        navigate_to_update_fragment()

        // Then Navigate to Edit fragment and fragment is shown
        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_edit_when_click_on_navigation_tool_bar_then_return_on_detail_fragment() {

        // Given Edit fragment
        launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_detail_fragment()

        try {
            navigate_to_update_fragment()
            onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

            // When click on Navigate Up Home icon
            click_on_navigate_up_button()

            uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                testApplication.resources.getResourceEntryName(R.id.detail_fragment))), 10000)

            // Then return on Detail fragment and fragment is shown
            onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

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

        onView(withId(R.id.layout_interest_points)).perform(scrollTo())

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

        onView(withId(R.id.street)).check(matches(withText(fakeProperties[itemPosition].address!!.street)))
        onView(withId(R.id.city)).check(matches(withText(fakeProperties[itemPosition].address!!.city)))
        onView(withId(R.id.postal_code)).check(matches(withText(fakeProperties[itemPosition].address!!.postalCode)))
        onView(withId(R.id.country)).check(matches(withText(fakeProperties[itemPosition].address!!.country)))
        onView(withId(R.id.state)).check(matches(withText(fakeProperties[itemPosition].address!!.state)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}