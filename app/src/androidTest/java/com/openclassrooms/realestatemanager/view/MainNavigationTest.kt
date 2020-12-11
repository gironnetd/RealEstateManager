package com.openclassrooms.realestatemanager.view

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.openclassrooms.realestatemanager.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class MainNavigationTest : BaseMainActivityTests() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @Test
    fun nav_from_bottom_navigation_view_to_real_estate_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withId(R.id.navigation_simulation), isDisplayed())).perform(click())
        if(navController.currentDestination?.id!! != R.id.navigation_simulation) {
            runOnUiThread {
                navController.navigate(R.id.navigation_simulation)
            }
        }
        onView(allOf(withId(R.id.navigation_list), isDisplayed()))
                .perform(click())
        assertEquals(navController.currentDestination?.id, R.id.navigation_list)
    }

    @Test
    fun nav_from_bottom_navigation_view_to_simulation_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withId(R.id.navigation_simulation), isDisplayed()))
                .perform(click())
        assertEquals(navController.currentDestination?.id, R.id.navigation_simulation)
    }

    @Test
    fun nav_from_add_floating_action_button_to_create_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(withId(R.id.create_floating_action_button)).perform(click())
        assertEquals(navController.currentDestination?.id, R.id.navigation_create)
    }

    @Test
    fun nav_from_navigation_view_to_real_estate_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }

        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())

        if(navController.currentDestination?.id!! == R.id.navigation_list) {
            runOnUiThread {
                navController.navigate(R.id.navigation_simulation)
            }
        }
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_list))
        assertEquals(navController.currentDestination?.id, R.id.navigation_list)
    }

    @Test
    fun nav_from_navigation_view_to_add_real_estate_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_create))
        assertEquals(navController.currentDestination?.id, R.id.navigation_create)
    }

    @Test
    fun nav_from_navigation_view_to_simulation_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(withContentDescription(
                activityScenario
                        .getToolbarNavigationContentDescription()
        )).perform(click())
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_simulation))
        assertEquals(navController.currentDestination?.id, R.id.navigation_simulation)
    }

    @Test
    fun nav_from_search_tool_bar_item_to_search_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(withId(R.id.navigation_search)).perform(click())
        assertEquals(navController.currentDestination?.id, R.id.navigation_search)
    }
}