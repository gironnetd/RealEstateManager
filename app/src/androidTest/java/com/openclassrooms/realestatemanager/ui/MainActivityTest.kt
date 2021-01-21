package com.openclassrooms.realestatemanager.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.NavigationMenuItemView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.util.Constants
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class MainActivityTest : BaseMainActivityTests() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

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
        onView(withId(R.id.navigation_search)).check(matches(isDisplayed()))
    }

    @Test
    fun is_home_icon_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun when_click_on_home_icon_then_open_navigation_view(){
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
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java),
                withId(R.id.navigation_real_estate)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_add_real_estate_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.navigation_create)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_simulation_line_is_displayed() {
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_simulation)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun is_bottom_navigation_view_displayed() {
        onView(allOf(isAssignableFrom(BottomNavigationView::class.java)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun when_click_on_real_estate_bottom_navigation_view_then_button_is_checked() {
        onView(allOf(withText(R.string.real_estate), isDisplayed()))
                .perform(click())
        onView(allOf(withId(R.id.navigation_simulation),
                isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_real_estate),
                isDisplayed())).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun when_click_on_simulation_bottom_navigation_view_then_button_is_checked() {
        onView(allOf(withText(R.string.simulation), isDisplayed()))
                .perform(click())
        onView(allOf(withId(R.id.navigation_real_estate),
                isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_simulation),
                isDisplayed())).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun is_add_real_estate_floating_action_button_displayed() {
        onView(allOf(`is`(instanceOf(FloatingActionButton::class.java))
        )).check(matches(isDisplayed()))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}