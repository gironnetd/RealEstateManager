package com.openclassrooms.realestatemanager.view.realestate

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.view.BaseMainActivityTests
import com.openclassrooms.realestatemanager.view.realestate.display.RealEstateMasterFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class RealEstateMasterFragmentTest : BaseMainActivityTests() {

    @Before
    public override fun setUp() {
        super.setUp()
        launchFragmentInContainer<RealEstateMasterFragment>(null,
                R.style.AppTheme, Lifecycle.State.RESUMED)
    }

    @Test
    fun when_click_on_list_button_then_list_button_is_selected() {
        onView(withId(R.id.list_view_button)).perform(click())
        onView(withId(R.id.list_view_button)).check(matches(isSelected()))
    }

    @Test
    fun when_click_on_map_button_then_map_button_is_selected() {
        onView(withId(R.id.map_view_button)).perform(click())
        onView(withId(R.id.map_view_button)).check(matches(isSelected()))
    }

    @Test
    fun when_list_button_is_selected_then_list_fragment_is_displayed() {
        onView(withId(R.id.list_view_button)).perform(click())
        onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun when_map_button_is_selected_then_map_fragment_is_displayed() {
        onView(withId(R.id.map_view_button)).perform(click())
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_list_fragment_is_displayed_when_click_on_map_button_then_map_fragment_is_displayed() {
        // Given
        onView(withId(R.id.list_view_button)).perform(click())
        onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))

        // When
        onView(withId(R.id.map_view_button)).perform(click())

        // Then
        onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
    }
}