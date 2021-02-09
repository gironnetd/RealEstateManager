package com.openclassrooms.realestatemanager.repository.property.properties

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.codingwithmitch.espressodaggerexamples.util.EspressoIdlingResourceRule
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.properties.PropertiesAdapter
import com.openclassrooms.realestatemanager.util.Constants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertiesFragmentIntegrationTest : BaseMainActivityTests() {

    @get: Rule
    val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    @Test
    fun is_property_list_empty() {

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

        launchActivity<MainActivity>()

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        recyclerView.check(matches(withEffectiveVisibility(Visibility.GONE)))

        Espresso.onView(withId(R.id.no_data_text_view))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun verify_properties_list_scrolling() {

        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = Constants.PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)
        injectTest(app)

        launchActivity<MainActivity>()

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        recyclerView.check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertiesAdapter.PropertyViewHolder>(5)
        )
        Espresso.onView(withText("21 Rue Kléber")).check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertiesAdapter.PropertyViewHolder>(8)
        )
        Espresso.onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertiesAdapter.PropertyViewHolder>(0)
        )
        Espresso.onView(withText("3 Square Fantin Latour")).check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.no_data_text_view))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun is_instance_state_saved_and_restored_on_destroy_activity() {

        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = Constants.PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        configureFakeRepository(apiService, app)
        injectTest(app)

        val scenario = launchActivity<MainActivity>()

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        Espresso.onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertiesAdapter.PropertyViewHolder>(8)
        )
        Espresso.onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))

        scenario.recreate()

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertiesAdapter.PropertyViewHolder>(8)
        )
        Espresso.onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))
    }


    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}