package com.openclassrooms.realestatemanager.data.repository.property

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests.ScreenSize.*
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class BrowseFragmentTest : BaseMainActivityTests() {

    // device size variables
    private var screenSize: ScreenSize = UNDEFINED

    // orientation variables
    private var orientation: Int = -1

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

        launch(MainActivity::class.java).onActivity {
            mainActivity = it as FragmentActivity
        }

        screenSize = screenSize()
        orientation = InstrumentationRegistry.getInstrumentation().targetContext
                .resources.configuration.orientation
    }

    @Test
    fun makeSureThatViewCorrespondWithSizeAndOrientation() {
        if(screenSize == SMARTPHONE && orientation == ORIENTATION_PORTRAIT) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }

        if(screenSize == SMARTPHONE && orientation == ORIENTATION_LANDSCAPE) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }

        if(screenSize == PHABLET && orientation == ORIENTATION_PORTRAIT) {
            onView(anyOf(withId(R.id.list_fragment),
                    withId(R.id.map_fragment)))
                    .check(matches(isDisplayed()))
        }

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

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}