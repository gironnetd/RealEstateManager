package com.openclassrooms.realestatemanager.ui.main

import android.view.View
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : BaseMainActivityTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    public override fun setUp() {
        super.setUp()

        configure_fake_repository()
        injectTest(testApplication)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun given_main_activity_when_launched_then_title_toolbar_displayed(){
        onView(allOf(withText(R.string.app_name), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_when_launched_then_search_item_displayed() {
        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(ActionMenuItemView::class.java), isDisplayed())).check(matches(isDisplayed()))
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
    fun given_main_activity_launched_when_navigation_view_is_opened_then_properties_line_is_displayed() {
        // Given Main activity is launched

        // When Navigation view is opened
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        // Then Real estate line is displayed
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_browse)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_navigation_view_is_opened_then_add_properties_line_is_displayed() {
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
    fun given_main_activity_launched_when_navigation_view_is_opened_then_search_properties_line_is_displayed() {
        // Given Main activity is launched

        // When Navigation view is opened
        onView(allOf(withContentDescription(
            activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
            .perform(click())

        // Then Search estate line is displayed
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_main_search)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_when_launched_then_bottom_navigation_view_displayed() {
        onView(allOf(isAssignableFrom(BottomNavigationView::class.java)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_click_on_properties_bottom_navigation_view_then_button_is_checked() {
        // Given Main activity is launched

        // When click on Real estate Bottom Navigation view
        onView(allOf(withId(R.id.navigation_browse), isDisplayed())).perform(click())

        // Then Real estate Bottom Navigation button is checked
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(BottomNavigationItemView::class.java), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_browse), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun given_main_activity_launched_when_click_on_create_bottom_navigation_view_then_button_is_checked() {
        // Given Main activity is launched

        // When click on Create Real estate Bottom Navigation view
        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        // Then Create Real estate Bottom Navigation button is checked
        onView(allOf(withId(R.id.navigation_browse), isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(BottomNavigationItemView::class.java) , isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java)))
            .check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun given_main_activity_launched_when_click_on_search_bottom_navigation_view_then_button_is_checked() {
        // Given Main activity is launched

        // When click on Create Real estate Bottom Navigation view
        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        // Then Search Real estate Bottom Navigation button is checked
        onView(allOf(withId(R.id.navigation_browse), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
            .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(BottomNavigationItemView::class.java)))
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