package com.openclassrooms.realestatemanager.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainNavigationTest : BaseMainActivityTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)
    }

    @Test
    fun given_main_activity_launched_when_click_on_real_estate_bottom_navigation_view_then_browse_fragment_is_shown() {
        // Given Main activity is launched
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())
        if (navController.currentDestination?.id!! != R.id.navigation_create) {
            runOnUiThread {
                navController.navigate(R.id.navigation_create)
            }
        }

        // When click on Real estate Bottom Navigation view
        onView(allOf(withId(R.id.navigation_real_estate), isDisplayed())).perform(click())

        // Then Browse fragment is shown
        assertEquals(navController.currentDestination?.id, R.id.navigation_real_estate)
    }

    @Test
    fun given_main_activity_launched_when_click_on_create_bottom_navigation_view_then_create_fragment_is_shown() {
        // Given Main activity is launched
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }

        // When click on Create Bottom Navigation view
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
                .perform(click())

        // Then Create fragment is shown
        onView(withId(R.id.create_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_click_on_real_estate_navigation_view_then_browse_fragment_is_shown() {
        // Given Main activity is launched
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        if(navController.currentDestination?.id!! == R.id.navigation_real_estate) {
            runOnUiThread {
                navController.navigate(R.id.navigation_create)
            }
        }

        // When click on Real estate Navigation view
        onView(withId(R.id.navigation_view))
            .perform(NavigationViewActions.navigateTo(R.id.navigation_real_estate))

        // Then Browse fragment is shown
        assertEquals(navController.currentDestination?.id, R.id.navigation_real_estate)
    }

    @Test
    fun given_main_activity_launched_when_click_on_create_navigation_view_then_create_fragment_is_shown() {
        // Given Main activity is launched
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        // When click on Create Navigation view
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_create))

        // Then Create fragment is shown
        onView(withId(R.id.create_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_main_activity_launched_when_click_on_search_tool_bar_item_then_search_fragment_is_shown() {
        // Given Main activity is launched
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }

        // When click on Search Toolbar item
        onView(withId(R.id.navigation_search)).perform(click())

        // Then Search fragment is shown
        onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}