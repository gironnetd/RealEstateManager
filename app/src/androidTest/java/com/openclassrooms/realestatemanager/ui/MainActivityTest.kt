package com.openclassrooms.realestatemanager.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenuItemView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : BaseMainActivityTests() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    val app = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext as TestBaseApplication

    @Before
    public override fun setUp() {
        super.setUp()

        val apiService = configureFakeApiService(
                propertiesDataSource = EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)
        injectTest(app)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun is_title_toolbar_displayed(){
        onView(allOf(withText(R.string.app_name), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun is_search_item_displayed() {
        onView(withId(R.id.navigation_search)).check(matches(isDisplayed()))
    }

    @Test
    fun is_home_icon_displayed() {
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .check(matches(isDisplayed()))
    }

    @Test
    fun when_click_on_home_icon_then_open_navigation_view(){
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())
        onView(withId(R.id.navigation_view)).check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_real_estate_line_is_displayed() {
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java),
                withId(R.id.navigation_real_estate)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_add_real_estate_line_is_displayed() {
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())
        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_real_estate)))
                .check(matches(isDisplayed()))
    }

    @Test
    fun when_navigation_view_is_opened_then_create_line_is_displayed() {
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.navigation_create)))
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
        onView(allOf(withId(R.id.navigation_create),
                isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_real_estate),
                isDisplayed())).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun when_click_on_create_bottom_navigation_view_then_button_is_checked() {
        onView(allOf(withText(R.string.create), isDisplayed()))
                .perform(click())
        onView(allOf(withId(R.id.navigation_real_estate),
                isDisplayed()))
                .check(matches(withBottomNavItemCheckedStatus(false)))
        onView(allOf(withId(R.id.navigation_create),
                isDisplayed())).check(matches(withBottomNavItemCheckedStatus(true)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}