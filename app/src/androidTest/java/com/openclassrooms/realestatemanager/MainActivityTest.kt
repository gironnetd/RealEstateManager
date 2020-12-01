package com.openclassrooms.realestatemanager

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import junit.framework.TestCase
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
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
        onView(withId(R.id.real_estate)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_add_real_estate_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.add_real_estate)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_simulation_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.simulation)).check(matches(isDisplayed()))
    }

    @Test
    fun is_bottom_navigation_view_displayed() {
        onView(allOf(`is`(instanceOf(BottomNavigationView::class.java))
                )).check(matches(isDisplayed()))
    }

    @Test
    fun when_click_on_real_estate_bottom_navigation_view_then_button_is_checked() {
        onView(withId(R.id.navigation_real_estate_view)).perform(click())
        onView(withId(R.id.navigation_simulation_view)).check(matches(withBottomNavItemCheckedStatus(false)))
        onView(withId(R.id.navigation_real_estate_view)).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun when_click_on_simulation_bottom_navigation_view_then_button_is_checked() {
        onView(withId(R.id.navigation_simulation_view)).perform(click())
        onView(withId(R.id.navigation_real_estate_view)).check(matches(withBottomNavItemCheckedStatus(false)))
        onView(withId(R.id.navigation_simulation_view)).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun is_add_real_estate_floating_action_button_displayed() {
        onView(allOf(`is`(instanceOf(FloatingActionButton::class.java))
        )).check(matches(isDisplayed()))
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

    private fun withBottomNavItemCheckedStatus(isChecked: Boolean): Matcher<View?>? {
        return object : BoundedMatcher<View?, BottomNavigationItemView>(BottomNavigationItemView::class.java) {
            var triedMatching = false
            override fun describeTo(description: Description) {
                if (triedMatching) {
                    description.appendText("with BottomNavigationItem check status: $isChecked")
                }
            }

            override fun matchesSafely(item: BottomNavigationItemView): Boolean {
                triedMatching = true
                return item.itemData.isChecked == isChecked
            }
        }
    }
}