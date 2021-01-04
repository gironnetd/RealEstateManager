package com.openclassrooms.realestatemanager.view.realestate

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.view.BaseMainActivityTests
import com.openclassrooms.realestatemanager.view.BaseMainActivityTests.ScreenSize.*
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class RealEstateFragmentTest : BaseMainActivityTests() {

    // device size variables
    private var screenSize : ScreenSize = UNDEFINED

    // orientation variables
    private var orientation : Int = -1

    @Before
    public override fun setUp() {
        super.setUp()
        launchFragmentInContainer<RealEstateFragment>(null
                , R.style.AppTheme,
                Lifecycle.State.RESUMED)
                .onFragment {
                    mainActivity = it.activity as FragmentActivity
        }

        screenSize = screenSize()
        orientation = InstrumentationRegistry.getInstrumentation().targetContext
                .resources.configuration.orientation
    }

    @Test
    fun when_smartphone_and_portrait_then_one_fragment_is_displayed() {
        if(screenSize == SMARTPHONE && orientation == ORIENTATION_PORTRAIT) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }
    }

    @Test
    fun when_smartphone_and_landscape_then_one_fragment_is_displayed() {
        if(screenSize == SMARTPHONE && orientation == ORIENTATION_LANDSCAPE) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }
    }

    @Test
    fun when_phablet_and_portrait_then_one_fragment_is_displayed() {
        if(screenSize == PHABLET && orientation == ORIENTATION_PORTRAIT) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }
    }

    @Test
    fun when_phablet_and_landscape_then_two_fragment_is_displayed() {
        if(screenSize == PHABLET && orientation == ORIENTATION_LANDSCAPE) {
            onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))

            onView(withId(R.id.list_fragment))
                    .check(isCompletelyLeftOf(
                            anyOf(withId(R.id.map_fragment),
                                    withId(R.id.detail_fragment))))

            onView(anyOf(withId(R.id.map_fragment),
                    withId(R.id.detail_fragment)))
                    .check(matches(isDisplayed()))
            }
    }

    @Test
    fun when_tablet_and_portrait_then_two_fragment_is_displayed() {
        if(screenSize == TABLET && orientation == ORIENTATION_PORTRAIT) {
            onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))

            onView(withId(R.id.list_fragment))
                    .check(isCompletelyLeftOf(
                            anyOf(withId(R.id.map_fragment),
                                    withId(R.id.detail_fragment))))

            onView(anyOf(withId(R.id.map_fragment),
                    withId(R.id.detail_fragment)))
                    .check(matches(isDisplayed()))
        }
    }

    @Test
    fun when_tablet_and_landscape_then_two_fragment_is_displayed() {
        if(screenSize == TABLET && orientation == ORIENTATION_LANDSCAPE) {
            onView(withId(R.id.list_fragment)).check(matches(isDisplayed()))

            onView(withId(R.id.list_fragment))
                    .check(isCompletelyLeftOf(
                            anyOf(withId(R.id.map_fragment),
                                    withId(R.id.detail_fragment))))

            onView(anyOf(withId(R.id.map_fragment),
                    withId(R.id.detail_fragment)))
                    .check(matches(isDisplayed()))
        }
    }
}