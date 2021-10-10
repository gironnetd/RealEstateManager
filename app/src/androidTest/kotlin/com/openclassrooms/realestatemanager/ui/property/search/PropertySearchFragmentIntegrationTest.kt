package com.openclassrooms.realestatemanager.ui.property.search

import android.view.View
import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.InterestPoint.*
import com.openclassrooms.realestatemanager.models.property.PropertyStatus.*
import com.openclassrooms.realestatemanager.models.property.PropertyType.*
import com.openclassrooms.realestatemanager.models.property.PropertyType.NONE
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
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
import org.hamcrest.core.AllOf
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
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment()
        }

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
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment()
        }

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
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment()
        }

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
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(5)
    @Test
    fun given_search_fragment_when_in_sale_button_is_checked_and_click_on_then_in_sale_text_input_layout_is_disabled() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(withId(R.id.in_sale_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(6)
    @Test
    fun given_search_fragment_when_click_on_for_rent_button_then_for_rent_text_input_layout_is_enabled() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(7)
    @Test
    fun given_search_fragment_when_for_rent_button_is_checked_and_click_on_then_for_rent_text_input_layout_is_disabled() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(withId(R.id.for_rent_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(8)
    @Test
    fun given_search_fragment_when_click_on_sold_button_then_sold_text_input_layouts_are_enabled() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(isEnabled()))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(isEnabled()))
    }

    @Order(9)
    @Test
    fun given_search_fragment_when_sold_button_is_checked_and_click_on_then_sold_text_input_layouts_are_disabled() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(withId(R.id.sold_entry_date_text_input_layout)).check(matches(not(isEnabled())))
        onView(withId(R.id.sold_date_text_input_layout)).check(matches(not(isEnabled())))
    }

    @Order(10)
    @Test
    fun given_search_fragment_when_click_on_flat_checkbox_then_flat_checkbox_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(FLAT)).isTrue()
    }

    @Order(11)
    @Test
    fun given_search_fragment_when_flat_checkbox_is_checked_and_click_on_then_flat_checkbox_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).perform(click())
        onView(withId(R.id.flat_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(FLAT)).isFalse()
    }

    @Order(12)
    @Test
    fun given_search_fragment_when_click_on_townhouse_check_box_then_townhouse_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(TOWNHOUSE)).isTrue()
    }

    @Order(13)
    @Test
    fun given_search_fragment_when_townhouse_check_box_is_checked_and_click_on_then_townhouse_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).perform(click())
        onView(withId(R.id.townhouse_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(TOWNHOUSE)).isFalse()
    }

    @Order(14)
    @Test
    fun given_search_fragment_when_click_on_penthouse_check_box_then_penthouse_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(PENTHOUSE)).isTrue()
    }

    @Order(15)
    @Test
    fun given_search_fragment_when_penthouse_check_box_is_checked_and_click_on_then_penthouse_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).perform(click())
        onView(withId(R.id.penthouse_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(PENTHOUSE)).isFalse()
    }

    @Order(16)
    @Test
    fun given_search_fragment_when_click_on_house_check_box_then_house_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(HOUSE)).isTrue()
    }

    @Order(17)
    @Test
    fun given_search_fragment_when_house_check_box_is_checked_and_click_on_then_house_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).perform(click())
        onView(withId(R.id.house_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(HOUSE)).isFalse()
    }

    @Order(18)
    @Test
    fun given_search_fragment_when_click_on_duplex_check_box_then_duplex_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(DUPLEX)).isTrue()
    }

    @Order(19)
    @Test
    fun given_search_fragment_when_duplex_check_box_is_checked_and_click_on_then_duplex_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).perform(click())
        onView(withId(R.id.duplex_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(DUPLEX)).isFalse()
    }

    @Order(20)
    @Test
    fun given_search_fragment_when_click_on_none_check_box_then_none_check_box_is_checked_and_added_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).check(matches(isChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(NONE)).isTrue()
    }

    @Order(21)
    @Test
    fun given_search_fragment_when_none_check_box_is_checked_and_click_on_then_none_check_box_is_unchecked_and_removed_to_selected_type_set() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).perform(click())
        onView(withId(R.id.none_checkbox)).check(matches(isNotChecked()))
        assertThat(propertySearchFragment.selectedTypes.contains(NONE)).isFalse()
    }

    @Order(22)
    @Test
    fun given_search_fragment_when_date_picker_dialog_shown_and_date_updated_then_initialize_edit_text_with_corresponding_date() {
        // Given Search fragment
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

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
        launchFragmentInContainer(fragmentArgs = null, R.style.AppTheme_Tertiary, Lifecycle.State.RESUMED) {
            PropertySearchFragment().also {
                propertySearchFragment = it
            }
        }

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

        val entryDate: Date = Utils.fromStringToDate(propertySearchFragment.binding.inSaleTextEdit!!.text.toString())
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

    @Order(24)
    @Test
    fun given_main_search_fragment_when_click_on_search_button_then_result_fragment_is_shown() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())
        onView(withId(R.id.browse_fragment)).check(matches(isDisplayed()))
    }

    @Order(25)
    @Test
    fun given_main_search_fragment_when_click_on_flat_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Flat CheckBox
        onView(withId(R.id.flat_checkbox)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.type == FLAT }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(26)
    @Test
    fun given_main_search_fragment_when_click_on_townhouse_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Townhouse CheckBox
        onView(withId(R.id.townhouse_checkbox)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.type == TOWNHOUSE }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(27)
    @Test
    fun given_main_search_fragment_when_click_on_penthouse_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Penthouse CheckBox
        onView(withId(R.id.penthouse_checkbox)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.type == PENTHOUSE }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(28)
    @Test
    fun given_main_search_fragment_when_click_on_house_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On House CheckBox
        onView(withId(R.id.house_checkbox)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.type == HOUSE }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(29)
    @Test
    fun given_main_search_fragment_when_click_on_duplex_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Duplex CheckBox
        onView(withId(R.id.duplex_checkbox)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.type == DUPLEX }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(30)
    @Test
    fun given_main_search_fragment_when_click_on_in_sale_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On In Sale Button, Entry Date and Enter value
        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(allOf(withId(R.id.in_sale_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == IN_SALE }.random().entryDate!!.time

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!
                .all { property -> property.status == IN_SALE && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(31)
    @Test
    fun given_main_search_fragment_when_click_on_for_rent_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On For Rent Button, Entry Date and Enter value
        onView(withId(R.id.for_rent_radio_button)).perform(click())
        onView(allOf(withId(R.id.for_rent_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == FOR_RENT }.random().entryDate!!.time

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!
                .all { property -> property.status == FOR_RENT && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(32)
    @Test
    fun given_main_search_fragment_when_click_on_sold_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Sold Button, Entry Date and Enter value
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(allOf(withId(R.id.sold_entry_date_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == SOLD }.random().entryDate!!.time

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!
                .all { property -> property.status == SOLD && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(33)
    @Test
    fun given_main_search_fragment_when_click_on_sold_button_and_sold_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Sold Button, Sold Date and Enter value
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(allOf(withId(R.id.sold_date_text_edit), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == SOLD }.random().soldDate!!.time

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!
                .all { property -> property.status == SOLD && property.soldDate!!.time <= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(34)
    @Test
    fun given_main_search_fragment_when_click_on_min_price_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Price and Enter value
        onView(withId(R.id.min_price)).perform(click())
        val minPrice = fakeProperties.random().price
        onView(withId(R.id.min_price)).perform(replaceText(minPrice.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.price >= minPrice }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(35)
    @Test
    fun given_main_search_fragment_when_click_on_max_price_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Price and Enter value
        onView(withId(R.id.max_price)).perform(click())
        val maxPrice = fakeProperties.random().price
        onView(withId(R.id.max_price)).perform(replaceText(maxPrice.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.price <= maxPrice }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(36)
    @Test
    fun given_main_search_fragment_when_click_on_min_surface_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Surface and Enter value
        onView(withId(R.id.min_surface)).perform(click())
        val minSurface = fakeProperties.random().surface
        onView(withId(R.id.min_surface)).perform(replaceText(minSurface.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.surface >= minSurface }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(37)
    @Test
    fun given_main_search_fragment_when_click_on_max_surface_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Surface and Enter value
        onView(withId(R.id.max_surface)).perform(click())
        val maxSurface = fakeProperties.random().surface
        onView(withId(R.id.max_surface)).perform(replaceText(maxSurface.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.surface <= maxSurface }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(38)
    @Test
    fun given_main_search_fragment_when_click_on_min_rooms_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Rooms and Enter value
        onView(withId(R.id.min_rooms)).perform(click())
        val minRooms = fakeProperties.random().rooms
        onView(withId(R.id.min_rooms)).perform(replaceText(minRooms.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.rooms >= minRooms }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(39)
    @Test
    fun given_main_search_fragment_when_click_on_max_rooms_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Rooms and Enter value
        onView(withId(R.id.max_rooms)).perform(click())
        val maxRooms = fakeProperties.random().rooms
        onView(withId(R.id.max_rooms)).perform(replaceText(maxRooms.toString()))

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.rooms <= maxRooms }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(40)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_school_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby School
        onView(withText(testApplication.resources.getString(R.string.interest_point_school))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(SCHOOL) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(41)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_playground_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Playground
        onView(withText(testApplication.resources.getString(R.string.interest_point_playground))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(PLAYGROUND) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(42)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_shop_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Shop
        onView(withText(testApplication.resources.getString(R.string.interest_point_shop))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(SHOP) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(43)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_buses_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Buses
        onView(withText(testApplication.resources.getString(R.string.interest_point_buses))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(BUSES) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(44)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_subway_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Subway
        onView(withText(testApplication.resources.getString(R.string.interest_point_subway))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(SUBWAY) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(45)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_park_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Park
        onView(withText(testApplication.resources.getString(R.string.interest_point_park))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(PARK) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(46)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_hospital_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Hospital
        onView(withText(testApplication.resources.getString(R.string.interest_point_hospital))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(HOSPITAL) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(47)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_restaurants_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Restaurants
        onView(withText(testApplication.resources.getString(R.string.interest_point_restaurants))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(RESTAURANTS) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    @Order(48)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_gas_stations_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Gas Stations
        onView(withText(testApplication.resources.getString(R.string.interest_point_gas_stations))).perform(click())

        // Click On Search Button
        onView(AllOf.allOf(withId(R.id.navigation_result_search), isDisplayed())).perform(click())

        // Then Properties are filtered
        searchedProperties.value?.let {
            assertThat(searchedProperties.value!!.all { property -> property.interestPoints.contains(GAS_STATIONS) }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.no_property_found)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}