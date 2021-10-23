package com.openclassrooms.realestatemanager.ui.property.browse

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.util.RxImmediateSchedulerRule
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class BrowseFragmentTest : BaseMainActivityTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    // device size variables
    private var screenSize: ScreenSize = UNDEFINED

    // orientation variables
    private var orientation: Int = -1

    @Before
    public override fun setUp() {
        super.setUp()

        configure_fake_repository()
        injectTest(testApplication)

        launch(MainActivity::class.java).onActivity {
            mainActivity = it as FragmentActivity
        }

        screenSize = screen_size()
        orientation = getInstrumentation().targetContext.resources.configuration.orientation
    }

    @Test
    fun given_main_activity_when_normal_mode_then_segmented_control_behavior_is_correct() {
        // Given Main Activity launched

        // When layout is in Normal mode
        if(!testApplication.resources.getBoolean(R.bool.isMasterDetail)) {

            // Then the behavior of SegmentedControl work correctly
            onView(withId(R.id.list_view_button)).perform(ViewActions.click())
            onView(withId(R.id.list_view_button)).check(matches(ViewMatchers.isSelected()))

            onView(withId(R.id.map_view_button)).perform(ViewActions.click())
            onView(withId(R.id.map_view_button)).check(matches(ViewMatchers.isSelected()))

            onView(withId(R.id.list_view_button)).perform(ViewActions.click())
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))

            onView(withId(R.id.map_view_button)).perform(ViewActions.click())
            onView(withId(R.id.map_fragment)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun given_main_activity_when_launched_then_view_correspond_with_size_and_orientation() {
        // Given Main Activity

        // When is launched

        // Then the view correspond with size and orientation layout
        if(screenSize == SMARTPHONE && orientation == ORIENTATION_PORTRAIT) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))
            onView(withId(R.id.segmentedcontrol)).check(matches(isDisplayed()))
        }

        if(screenSize == SMARTPHONE && orientation == ORIENTATION_LANDSCAPE) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))
            onView(withId(R.id.segmentedcontrol)).check(matches(isDisplayed()))
        }

        if(screenSize == PHABLET && orientation == ORIENTATION_PORTRAIT) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))
            onView(withId(R.id.segmentedcontrol)).check(matches(isDisplayed()))
        }

        if(screenSize == PHABLET && orientation == ORIENTATION_LANDSCAPE) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))

            onView(withId(R.id.properties_recycler_view))
                    .check(isCompletelyLeftOf(
                            anyOf(withId(R.id.map_fragment),
                                    withId(R.id.edit_fragment))))

            onView(anyOf(withId(R.id.map_fragment),
                    withId(R.id.edit_fragment)))
                    .check(matches(isDisplayed()))
        }

        if(screenSize == TABLET && orientation == ORIENTATION_PORTRAIT) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))
            onView(withId(R.id.segmentedcontrol)).check(matches(isDisplayed()))
        }

        if(screenSize == TABLET && orientation == ORIENTATION_LANDSCAPE) {
            onView(withId(R.id.properties_recycler_view)).check(matches(isDisplayed()))

            onView(withId(R.id.properties_recycler_view))
                    .check(isCompletelyLeftOf(
                            anyOf(withId(R.id.map_fragment),
                                    withId(R.id.edit_fragment))))

            onView(anyOf(withId(R.id.map_fragment),
                    withId(R.id.edit_fragment)))
                    .check(matches(isDisplayed()))
        }
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}