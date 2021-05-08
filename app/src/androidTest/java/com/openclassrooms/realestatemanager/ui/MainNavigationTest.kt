package com.openclassrooms.realestatemanager.ui

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
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainNavigationTest : BaseMainActivityTests() {

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @Before
    public override fun setUp() {
        super.setUp()

        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)
        injectTest(app)
    }


    @Test
    fun nav_from_bottom_navigation_view_to_real_estate_fragment() {
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
        onView(allOf(withId(R.id.navigation_real_estate), isDisplayed()))
                .perform(click())

        assertEquals(navController.currentDestination?.id, R.id.navigation_real_estate)
    }

    @Test
    fun nav_from_bottom_navigation_view_to_create_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withId(R.id.navigation_create), isDisplayed()))
                .perform(click())
        onView(withId(R.id.create_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun nav_from_navigation_view_to_real_estate_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }

        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())

        if(navController.currentDestination?.id!! == R.id.navigation_real_estate) {
            runOnUiThread {
                navController.navigate(R.id.navigation_create)
            }
        }

        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_real_estate))

        onView(isRoot()).perform(waitFor(1000))

        assertEquals(navController.currentDestination?.id, R.id.navigation_real_estate)
    }

    @Test
    fun nav_from_navigation_view_to_add_real_estate_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_create))
        onView(withId(R.id.create_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun nav_from_navigation_view_to_create_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(allOf(withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()), isDisplayed()))
                .perform(click())
        onView(withId(R.id.navigation_view))
                .perform(NavigationViewActions.navigateTo(R.id.navigation_create))
        onView(withId(R.id.create_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun nav_from_search_tool_bar_item_to_search_fragment() {
        activityScenario = launch(MainActivity::class.java)
                .onActivity { mainActivity ->
                    navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment)
                }
        onView(withId(R.id.navigation_search)).perform(click())
        onView(withId(R.id.search_fragment)).check(matches(isDisplayed()))

    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}