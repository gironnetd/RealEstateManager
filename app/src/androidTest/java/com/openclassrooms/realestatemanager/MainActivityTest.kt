package com.openclassrooms.realestatemanager

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : TestCase() {

    @Before
    public override fun setUp() {
        super.setUp()
        ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    public override fun tearDown() {}

    @Test
    fun verify_if_title_toolbar_is_displayed(){
        onView(withText(R.string.app_name)).check(matches(isDisplayed()))
    }

    @Test
    fun verify_if_search_item_is_displayed() {
        onView(withId(R.id.search)).check(matches(isDisplayed()))
    }
}