package com.openclassrooms.realestatemanager.ui

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenuItemView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : BaseMainActivityTests() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    public override fun setUp() {
        super.setUp()

        configure_fake_repository(apiService = configure_fake_api_service(
            propertiesDataSource = EMPTY_LIST, // empty list of data
            networkDelay = 0L)
        )
        injectTest(testApplication)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun given_main_activity_when_launched_then_title_toolbar_displayed(){
        onView(allOf(withText(R.string.app_name), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_when_launched_then_search_item_displayed() {
        onView(withId(R.id.navigation_search)).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_when_launched_then_home_icon_displayed() {
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_click_on_home_icon_then_open_navigation_view(){
        // Given Main activity is launched

        // When click on Home icon
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        // Then Navigation view is opened
        onView(withId(R.id.navigation_view)).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_navigation_view_is_opened_then_real_estate_line_is_displayed() {
        // Given Main activity is launched

        // When Navigation view is opened
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        // Then Real estate line is displayed
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_real_estate)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_navigation_view_is_opened_then_add_real_estate_line_is_displayed() {
        // Given Main activity is launched

        // When Navigation view is opened
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        // Then Create estate line is displayed
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_create)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_when_launched_then_bottom_navigation_view_displayed() {
        onView(allOf(isAssignableFrom(BottomNavigationView::class.java)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_click_on_real_estate_bottom_navigation_view_then_button_is_checked() {
        // Given Main activity is launched

        // When click on Real estate Bottom Navigation view
        onView(allOf(withText(R.string.real_estate), isDisplayed())).perform(click())

        // Then Real estate Bottom Navigation button is checked
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_real_estate), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun given_main_activity_launched_when_click_on_create_bottom_navigation_view_then_button_is_checked() {
        // Given Main activity is launched

        // When click on Create Real estate Bottom Navigation view
        onView(allOf(withText(R.string.create), isDisplayed())).perform(click())

        // Then Create Real estate Bottom Navigation button is checked
        onView(allOf(withId(R.id.navigation_real_estate), isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(true)))
    }

    private fun withBottomNavItemCheckedStatus(isChecked: Boolean): Matcher<View?> {
        return object : BoundedMatcher<View?, BottomNavigationItemView>(BottomNavigationItemView::class.java) {
            var triedMatching = false
            override fun describeTo(description: Description) {
                if (triedMatching) {
                    description.appendText("with BottomNavigationItem check status: $isChecked")
                }
            }
            override fun matchesSafely(item: BottomNavigationItemView): Boolean {
                triedMatching = true
                return item.itemData!!.isChecked == isChecked
            }
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}