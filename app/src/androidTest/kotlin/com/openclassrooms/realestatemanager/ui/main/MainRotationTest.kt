package com.openclassrooms.realestatemanager.ui.main

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.util.OrientationChangeAction
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainRotationTest : BaseMainActivityTests() {

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
    fun given_create_fragment_displayed_when_rotation_then_display_create_fragment() {
        // Given Create fragment
        activityScenario = launch(MainActivity::class.java)
                .onActivity { activity ->
                    navController = findNavController(activity, R.id.nav_host_fragment)
                    mainActivity = activity
                }

        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
                .perform(click())
        onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

        // When a rotation occurs
        // Then Create fragment is displayed
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
        }
        if(orientation == ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))

            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            onView(withId(R.id.edit_fragment)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_search_fragment_displayed_when_rotation_then_display_main_search_fragment() {
        // Given Search fragment
        activityScenario = launch(MainActivity::class.java)
                .onActivity { activity ->
                    navController = findNavController(activity, R.id.nav_host_fragment)
                    mainActivity = activity
                }

        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(ActionMenuItemView::class.java), isDisplayed())).perform(click())
        onView(withId(R.id.main_search_fragment)).check(matches(isDisplayed()))

        // When a rotation occurs
        // Then Search fragment is displayed
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            onView(withId(R.id.main_search_fragment)).check(matches(isDisplayed()))

            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            onView(withId(R.id.main_search_fragment)).check(matches(isDisplayed()))
        }
        if(orientation == ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            onView(withId(R.id.main_search_fragment)).check(matches(isDisplayed()))

            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            onView(withId(R.id.main_search_fragment)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_main_search_fragment_displayed_when_browse_search_result_fragment_displayed_and_rotation_then_display_browse_fragment() {
        // Given Browse fragment
        activityScenario = launch(MainActivity::class.java)
            .onActivity { activity ->
                navController = findNavController(activity, R.id.nav_host_fragment)
                mainActivity = activity
            }

        onView(allOf(withId(R.id.navigation_main_search), isAssignableFrom(ActionMenuItemView::class.java), isDisplayed())).perform(click())
        onView(withId(R.id.menu_item_container)).perform(click())

        is_this_the_correct_fragment_displayed()

        // When a rotation occurs
        // Then Search fragment is displayed
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_main_search)
            is_this_the_correct_fragment_displayed()

            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_main_search)
            is_this_the_correct_fragment_displayed()
        }

        if(orientation == ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_main_search)
            is_this_the_correct_fragment_displayed()

            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_main_search)
            is_this_the_correct_fragment_displayed()
        }
    }

    @Test
    fun given_browse_fragment_displayed_when_rotation_then_display_browse_fragment() {
        // Given Browse fragment
        activityScenario = launch(MainActivity::class.java)
                .onActivity { activity ->
                    navController = findNavController(activity, R.id.nav_host_fragment)
                    mainActivity = activity
                }

        onView(allOf(withId(R.id.navigation_browse), isDisplayed())).perform(click())

        is_this_the_correct_fragment_displayed()

        // When a rotation occurs
        // Then Browse fragment is displayed
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if(orientation == ORIENTATION_PORTRAIT) {
            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_browse)
            is_this_the_correct_fragment_displayed()

            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            is_this_the_correct_fragment_displayed()
        }

        if(orientation == ORIENTATION_LANDSCAPE) {
            onView(isRoot()).perform(OrientationChangeAction.orientationPortrait(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_browse)
            is_this_the_correct_fragment_displayed()

            onView(isRoot()).perform(OrientationChangeAction.orientationLandscape(mainActivity))
            assertEquals(navController.currentDestination?.id, R.id.navigation_browse)
            is_this_the_correct_fragment_displayed()
        }
    }

    private fun is_this_the_correct_fragment_displayed() {
        sleep(1000)
        val isTablet = InstrumentationRegistry.getInstrumentation()
                .targetContext.resources.getBoolean(R.bool.isMasterDetail)

        if (isTablet) {
            onView(allOf(withId(R.id.properties_recycler_view), isDisplayed())).check(matches(isDisplayed()))

            onView(allOf(withId(R.id.properties_recycler_view), isDisplayed()))
                    .check(isCompletelyLeftOf(
                            anyOf(allOf(withId(R.id.map_fragment), isDisplayed()),
                                allOf(withId(R.id.edit_fragment), isDisplayed()))))

            onView(anyOf(allOf(withId(R.id.map_fragment), isDisplayed()),
                allOf(withId(R.id.edit_fragment), isDisplayed())))
                    .check(matches(isDisplayed()))
        }
        if(!isTablet) {
            onView(allOf(withId(R.id.properties_recycler_view), isDisplayed())).check(matches(isDisplayed()))
            onView(allOf(withId(R.id.list_view_button), withParent(allOf(withId(R.id.segmentedcontrol), isDisplayed())))).check(matches(isDisplayed()))
            onView(allOf(withId(R.id.map_view_button), withParent(allOf(withId(R.id.segmentedcontrol), isDisplayed())))).check(matches(isDisplayed()))
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}