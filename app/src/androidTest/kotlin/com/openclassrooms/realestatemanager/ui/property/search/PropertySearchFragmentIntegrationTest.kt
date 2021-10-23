package com.openclassrooms.realestatemanager.ui.property.search

import android.view.View
import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.PropertyType.*
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.fragments.MainNavHostFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment.Companion.searchedProperties
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.*
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(OrderedRunner::class)
@MediumTest
class PropertySearchFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var propertySearchFragment: PropertySearchFragment

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()

        BaseFragment.properties.value = fakeProperties.toMutableList()
        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        if (BaseFragment.properties.value != null) {
            BaseFragment.properties.value!!.clear()
        }

        if(searchedProperties.value != null) {
            searchedProperties.value!!.clear()
        }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        Single.fromCallable { Utils.isInternetAvailable() }
            .doOnSuccess { isInternetAvailable ->
                if (!isInternetAvailable) {
                    Completable.concatArray(
                        ConnectivityUtil.switchAllNetworks(true),
                        ConnectivityUtil.waitInternetStateChange(true)
                    )
                        .blockingAwait().let {
                            super.tearDown()
                        }
                } else { super.tearDown() }
            }.subscribeOn(SchedulerProvider.io()).blockingGet()
    }

    @Order(1)
    @Test
    fun given_search_fragment_when_click_on_all_type_then_all_types_are_checked_or_not() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val clickAction = object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isEnabled()
            }

            override fun getDescription(): String {
                return "click plus button"
            }

            override fun perform(uiController: UiController?, view: View) {
                view.performClick()
            }
        }

        onView(withId(R.id.all_type)).perform(clickAction)

        onView(withId(R.id.flat_checkbox)).check(matches(isChecked()))
        onView(withId(R.id.townhouse_checkbox)).check(matches(isChecked()))
        onView(withId(R.id.penthouse_checkbox)).check(matches(isChecked()))
        onView(withId(R.id.house_checkbox)).check(matches(isChecked()))
        onView(withId(R.id.duplex_checkbox)).check(matches(isChecked()))

        onView(allOf(withId(R.id.all_type))).perform(clickAction)

        onView(withId(R.id.flat_checkbox)).check(matches(isNotChecked()))
        onView(withId(R.id.townhouse_checkbox)).check(matches(isNotChecked()))
        onView(withId(R.id.penthouse_checkbox)).check(matches(isNotChecked()))
        onView(withId(R.id.house_checkbox)).check(matches(isNotChecked()))
        onView(withId(R.id.duplex_checkbox)).check(matches(isNotChecked()))
    }

    @Order(2)
    @Test
    fun given_search_fragment_when_click_on_status_radio_button_then_only_one_is_checked() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.in_sale_radio_button)).perform(click())

        onView(withId(R.id.in_sale_radio_button)).check(matches(isChecked()))
        onView(withId(R.id.for_rent_radio_button)).check(matches(isNotChecked()))
        onView(withId(R.id.sold_radio_button)).check(matches(isNotChecked()))

        onView(withId(R.id.for_rent_radio_button)).perform(click())

        onView(withId(R.id.in_sale_radio_button)).check(matches(isNotChecked()))
        onView(withId(R.id.for_rent_radio_button)).check(matches(isChecked()))
        onView(withId(R.id.sold_radio_button)).check(matches(isNotChecked()))

        onView(withId(R.id.sold_radio_button)).perform(click())

        onView(withId(R.id.in_sale_radio_button)).check(matches(isNotChecked()))
        onView(withId(R.id.for_rent_radio_button)).check(matches(isNotChecked()))
        onView(withId(R.id.sold_radio_button)).check(matches(isChecked()))
    }

    @Order(3)
    @Test
    fun given_search_fragment_when_click_on_status_radio_button_then_only_corresponding_text_input_layout_enabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.in_sale_radio_button)).perform(click())

        onView(withId(R.id.in_sale_text_input_layout)).check(matches(isEnabled()))
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(not(isEnabled())))

        onView(withId(R.id.for_rent_radio_button)).perform(click())

        onView(withId(R.id.in_sale_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(isEnabled()))
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(not(isEnabled())))

        onView(withId(R.id.sold_radio_button)).perform(click())

        onView(withId(R.id.in_sale_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(isEnabled()))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(4)
    @Test
    fun given_search_fragment_when_click_on_in_sale_button_then_in_sale_text_input_layout_is_enabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(5)
    @Test
    fun given_search_fragment_when_in_sale_button_is_checked_and_click_on_then_in_sale_text_input_layout_is_disabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(6)
    @Test
    fun given_search_fragment_when_click_on_for_rent_button_then_for_rent_text_input_layout_is_enabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(7)
    @Test
    fun given_search_fragment_when_for_rent_button_is_checked_and_click_on_then_for_rent_text_input_layout_is_disabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(8)
    @Test
    fun given_search_fragment_when_click_on_sold_button_then_sold_text_input_layouts_are_enabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(isEnabled()))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(9)
    @Test
    fun given_search_fragment_when_sold_button_is_checked_and_click_on_then_sold_text_input_layouts_are_disabled() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(10)
    @Test
    fun given_search_fragment_when_click_on_flat_checkbox_then_flat_checkbox_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(FLAT)).isTrue()
    }

    @Order(11)
    @Test
    fun given_search_fragment_when_flat_checkbox_is_checked_and_click_on_then_flat_checkbox_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(FLAT)).isFalse()
    }

    @Order(12)
    @Test
    fun given_search_fragment_when_click_on_townhouse_check_box_then_townhouse_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(TOWNHOUSE)).isTrue()
    }

    @Order(13)
    @Test
    fun given_search_fragment_when_townhouse_check_box_is_checked_and_click_on_then_townhouse_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(TOWNHOUSE)).isFalse()
    }

    @Order(14)
    @Test
    fun given_search_fragment_when_click_on_penthouse_check_box_then_penthouse_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(PENTHOUSE)).isTrue()
    }

    @Order(15)
    @Test
    fun given_search_fragment_when_penthouse_check_box_is_checked_and_click_on_then_penthouse_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(PENTHOUSE)).isFalse()
    }

    @Order(16)
    @Test
    fun given_search_fragment_when_click_on_house_check_box_then_house_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(HOUSE)).isTrue()
    }

    @Order(17)
    @Test
    fun given_search_fragment_when_house_check_box_is_checked_and_click_on_then_house_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(HOUSE)).isFalse()
    }

    @Order(18)
    @Test
    fun given_search_fragment_when_click_on_duplex_check_box_then_duplex_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(DUPLEX)).isTrue()
    }

    @Order(19)
    @Test
    fun given_search_fragment_when_duplex_check_box_is_checked_and_click_on_then_duplex_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(DUPLEX)).isFalse()
    }

    @Order(20)
    @Test
    fun given_search_fragment_when_click_on_none_check_box_then_none_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(NONE)).isTrue()
    }

    @Order(21)
    @Test
    fun given_search_fragment_when_none_check_box_is_checked_and_click_on_then_none_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(NONE)).isFalse()
    }

    @Order(22)
    @Test
    fun given_search_fragment_when_date_picker_dialog_shown_and_date_updated_then_initialize_edit_text_with_corresponding_date() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(allOf(withId(R.id.in_sale_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        onView(withId(R.id.in_sale_text_edit)).check(matches(withText(Utils.formatDate(calendar.time))))
    }

    @Order(23)
    @Test
    fun given_search_fragment_when_date_picker_dialog_shown_then_initialize_with_corresponding_date() {
        // Given Search fragment
        launch(MainActivity::class.java).onActivity { mainActivity = it }

        onView(
            allOf(
                withId(R.id.navigation_main_search),
                isAssignableFrom(BottomNavigationItemView::class.java),
                isDisplayed()
            )
        ).perform(click())

        val mainSearchFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment
                as MainNavHostFragment)
            .childFragmentManager.primaryNavigationFragment as MainSearchFragment
        propertySearchFragment = mainSearchFragment.mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(allOf(withId(R.id.in_sale_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        onView(allOf(withId(R.id.in_sale_text_edit), isDisplayed())).perform(click())

        val entryDate: Date = Utils.fromStringToDate(propertySearchFragment.binding.inSaleTextEdit.text.toString())
        calendar.time = entryDate

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .check(
                matches(
                    object : BoundedMatcher<View, DatePicker>(DatePicker::class.java) {
                        override fun describeTo(description: Description?) {}

                        override fun matchesSafely(item: DatePicker?): Boolean {
                            return ( calendar[Calendar.YEAR] == item?.year && calendar[Calendar.MONTH] == item.month && calendar[Calendar.DAY_OF_MONTH] == item.dayOfMonth)
                        }
                    })
            )
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}