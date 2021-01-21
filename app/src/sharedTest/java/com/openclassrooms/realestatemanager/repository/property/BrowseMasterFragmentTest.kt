package com.openclassrooms.realestatemanager.repository.property

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.util.Constants
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class BrowseMasterFragmentTest : BaseMainActivityTests() {

    @Before
    public override fun setUp() {
        super.setUp()

        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = Constants.EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)

        injectTest(app)

        launchFragmentInContainer<BrowseMasterFragment>(null,
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

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}