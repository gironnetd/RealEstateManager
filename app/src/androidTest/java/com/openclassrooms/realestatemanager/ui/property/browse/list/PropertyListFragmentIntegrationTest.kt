package com.openclassrooms.realestatemanager.ui.property.browse.list

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.codingwithmitch.espressodaggerexamples.util.FakeGlideRequestManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseMainActivityTests
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyListFragmentIntegrationTest : BaseMainActivityTests() {

    @get: Rule val espressoIdlingResourceRule = EspressoIdlingResourceRule()

    lateinit var requestManager: FakeGlideRequestManager
    lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory

    @Test
    fun is_property_list_empty() {
        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        val apiService = configureFakeApiService(
                propertiesDataSource = EMPTY_LIST, // empty list of data
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyListFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            mainActivity = it.activity as FragmentActivity
        }

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
                propertiesDataSource = PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyListFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            mainActivity = it.activity as FragmentActivity
        }

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        recyclerView.check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertyListAdapter.PropertyViewHolder>(2)
        )
        Espresso.onView(withText("2-8 Square de Castiglione")).check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertyListAdapter.PropertyViewHolder>(8))

        Espresso.onView(withText("3 Place de la Loi")).check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertyListAdapter.PropertyViewHolder>(0))

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
                propertiesDataSource = PROPERTIES_DATA_FILENAME,
                networkDelay = 0L,
                application = app
        )

        val propertiesRepository = configureFakeRepository(apiService, app)
        injectTest(app)

        requestManager = FakeGlideRequestManager()
        propertiesViewModelFactory = FakePropertiesViewModelFactory(propertiesRepository)

        val scenario = launchFragmentInContainer(null, R.style.AppTheme,
                Lifecycle.State.RESUMED) {
            PropertyListFragment(propertiesViewModelFactory, requestManager)
        }.onFragment {
            mainActivity = it.activity as FragmentActivity
        }

        val recyclerView = Espresso.onView(withId(R.id.recycler_view))

        Espresso.onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()))

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertyListAdapter.PropertyViewHolder>(8)
        )
        Espresso.onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))

        scenario.recreate()

        recyclerView.perform(
                RecyclerViewActions.scrollToPosition<PropertyListAdapter.PropertyViewHolder>(8)
        )
        Espresso.onView(withText("2 Avenue Jeanne d'Arc")).check(matches(isDisplayed()))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}