package com.openclassrooms.realestatemanager

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : TestCase() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    public override fun setUp() {
        super.setUp()
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    public override fun tearDown() {}

    @Test
    fun is_title_toolbar_displayed(){
        onView(withText(R.string.app_name)).check(matches(isDisplayed()))
    }

    @Test
    fun is_search_item_displayed() {
        onView(withId(R.id.search)).check(matches(isDisplayed()))
    }

    @Test
    fun is_home_icon_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun when_click_on_home_icon_then_open_navigation_view_(){
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.navigation_view)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_real_estate_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withText(R.string.real_estate)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_add_real_estate_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withText(R.string.add_real_estate)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_simulation_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withText(R.string.simulation)).check(matches(isDisplayed()))
    }

    private fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
            : String {
        var description = ""
        onActivity {
            description =
                    it.findViewById<Toolbar>(R.id.tool_bar).navigationContentDescription as String
        }
        return description
    }
}