package com.openclassrooms.realestatemanager.ui.property.edit.update

import android.view.View
import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.chip.Chip
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.InterestPoint
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.models.property.PropertyStatus
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.util.EnterPropertyUtil.update_property
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.RxImmediateSchedulerRule
import com.openclassrooms.realestatemanager.util.ToastMatcher
import com.openclassrooms.realestatemanager.util.Utils
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable.concatArray
import io.reactivex.Single
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyUpdateFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var propertyUpdateFragment: PropertyUpdateFragment

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        itemPosition = (fakeProperties.indices).random()
        BaseFragment.properties.value = fakeProperties.toMutableList()
        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        if(BaseFragment.properties.value != null) { BaseFragment.properties.value!!.clear() }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        Single.fromCallable { Utils.isInternetAvailable() }
            .doOnSuccess { isInternetAvailable ->
                if (!isInternetAvailable) {
                    concatArray(switchAllNetworks(true),
                        waitInternetStateChange(true))
                        .blockingAwait().let {
                            super.tearDown()
                        }
                } else { super.tearDown() }
            }.subscribeOn(SchedulerProvider.io()).blockingGet()
    }

    @Test
    fun given_update_when_click_sold_in_alert_dialog_then_sold_date_view_is_shown() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.status), withEffectiveVisibility(VISIBLE))).perform(scrollTo(), click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_click_for_rent_in_alert_dialog_then_sold_date_view_is_not_shown() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.status), withEffectiveVisibility(VISIBLE))).perform(scrollTo(), click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.FOR_RENT.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun given_update_when_click_on_in_sale_in_alert_dialog_then_sold_date_view_is_not_shown() {
        // Given Update fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.status), withEffectiveVisibility(VISIBLE))).perform(scrollTo(), click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.IN_SALE.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(doesNotExist())
    }

    @Test
    fun given_update_when_navigate_on_update_fragment_then_update_menu_item_is_shown() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(withId(R.id.navigation_update)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_property_is_updated_then_return_on_detail_fragment() {
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        update_property(testApplication = testApplication)

        onView(withId(R.id.navigation_update)).perform(click())
        onView(withText(R.string.confirm_save_changes)).perform(click())
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
    }

    //@Test
    fun given_update_when_return_on_detail_after_update_then_detail_property_is_updated_too() {
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        update_property(testApplication = testApplication)

        onView(withId(R.id.navigation_update)).perform(click())
        onView(withText(R.string.confirm_save_changes)).perform(click())
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))

        assertThat(obtainDetailFragment().property).isEqualTo(
            BaseFragment.properties.value!!.find { property -> property.id == obtainDetailFragment().property.id })
    }

    @Test
    fun given_update_when_on_back_pressed_then_confirm_dialog_is_shown() {
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()
        update_property(testApplication = testApplication)

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_save_changes_dialog_title)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.confirm_save_changes_dialog_message)).inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_on_back_pressed_and_click_confirm_then_return_to_detail_fragment() {
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        update_property(testApplication = testApplication)

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_save_changes)).inRoot(isDialog()).perform(click())
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_has_no_internet_a_message_indicating_property_is_updated_and_saved_only_on_local_storage_is_shown() {

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .blockingAwait().let {
                BaseFragment.properties.value = fakeProperties as MutableList<Property>
                launch(MainActivity::class.java).onActivity {
                    mainActivity = it
                    browseFragment = BrowseFragment()
                    it.setFragment(browseFragment)
                }
                isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

                navigate_to_update_fragment()
                update_property(testApplication = testApplication)

                onView(withId(R.id.navigation_update)).perform(click())
                onView(withText(R.string.confirm_save_changes)).perform(click())

                onView(withText(R.string.property_update_locally))
                    .inRoot(ToastMatcher().apply {
                        matches(isDisplayed())
                    })
            }
    }

    @Test
    fun given_update_when_has_no_internet_and_property_updated_when_has_internet_then_a_message_indicating_property_is_totally_saved_is_shown() {

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .blockingAwait().let {
                BaseFragment.properties.value = fakeProperties as MutableList<Property>
                launch(MainActivity::class.java).onActivity {
                    mainActivity = it
                    browseFragment = BrowseFragment()
                    it.setFragment(browseFragment)
                }
                isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

                navigate_to_update_fragment()
                update_property(testApplication = testApplication)

                onView(withId(R.id.navigation_update)).perform(click())
                onView(withText(R.string.confirm_save_changes)).perform(click())

                onView(withText(R.string.property_update_locally))
                    .inRoot(ToastMatcher().apply {
                        matches(isDisplayed())
                    })

                concatArray(switchAllNetworks(true), waitInternetStateChange(true))
                    .delay(Constants.TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                    .blockingAwait().let {

                        onView(withText(R.string.property_update_totally))
                            .inRoot(ToastMatcher().apply {
                                matches(isDisplayed())
                            })
                    }
            }
    }

    @Test
    fun given_update_when_is_shown_then_data_is_successfully_displayed_in_fragment() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.description), isDisplayed())).check(matches(withText(containsString(fakeProperties[itemPosition].description))))
        onView(allOf(withId(R.id.entry_date), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo())
            .check(matches(withText(Utils.formatDate(fakeProperties[itemPosition].entryDate))))

        if(fakeProperties[itemPosition].soldDate != null) {
            onView(allOf(withId(R.id.sold_date), withEffectiveVisibility(VISIBLE))).check(matches(
                withText(Utils.formatDate(fakeProperties[itemPosition].soldDate) ?:
                testApplication.resources.getString(fakeProperties[itemPosition].status.status))))
        } else {
            onView(allOf(withId(R.id.sold_date_text_input_layout),
                withParent(allOf(withId(R.id.container), isDisplayed()))))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
        onView(allOf(withId(R.id.interest_points_chip_group), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        InterestPoint.values().filter { interestPoint ->  interestPoint != InterestPoint.NONE }.forEachIndexed { index, interestPoint ->
            val chip: Chip = propertyUpdateFragment.binding.interestPointsChipGroup.getChildAt(index) as Chip
            var interestPointValue = ""
            fakeProperties[itemPosition].interestPoints.forEach {
                if(it == interestPoint) {
                    interestPointValue = testApplication.resources.getString(it.place)
                }
            }
            if(chip.isChecked) {
                assertThat(chip.text).isEqualTo(interestPointValue)
            } else {
                assertThat(chip.text).isNotEqualTo(interestPointValue)
            }
        }

        fakeProperties[itemPosition].interestPoints.forEach { interestPoint ->
            onView( allOf(withText(interestPoint.place), withEffectiveVisibility(VISIBLE))).check(matches(isDisplayed()))
        }

        onView(allOf(withId(R.id.price), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(containsString(fakeProperties[itemPosition].price.toString()))))
        onView(allOf(withId(R.id.type), isDisplayed())).check(matches(withText(fakeProperties[itemPosition].type.type)))

        onView(allOf(withId(R.id.surface), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(containsString(fakeProperties[itemPosition].surface.toString()))))
        onView(allOf(withId(R.id.rooms), isDisplayed())).check(matches(withText(fakeProperties[itemPosition].rooms.toString())))

        onView(allOf(withId(R.id.bathrooms), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(fakeProperties[itemPosition].bathRooms.toString())))
        onView(allOf(withId(R.id.bedrooms), isDisplayed())).check(matches(withText(fakeProperties[itemPosition].bedRooms.toString())))

        onView(allOf(withId(R.id.street), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(fakeProperties[itemPosition].address.street)))
        onView(allOf(withId(R.id.city), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(fakeProperties[itemPosition].address.city)))
        onView(allOf(withId(R.id.postal_code), isDisplayed())).check(matches(withText(fakeProperties[itemPosition].address.postalCode)))
        onView(allOf(withId(R.id.country), withEffectiveVisibility(VISIBLE))).perform(scrollTo()).check(matches(withText(fakeProperties[itemPosition].address.country)))
        onView(allOf(withId(R.id.state), isDisplayed())).check(matches(withText(fakeProperties[itemPosition].address.state)))
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_adapter_is_not_null() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter).isNotNull()
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_is_not_empty() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount).isNotEqualTo(0)
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_count_is_equal_to_property_photo_count() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount)
            .isEqualTo(fakeProperties[itemPosition].photos.size)
    }

    @Test
    fun given_update_when_entry_date_picker_dialog_shown_then_initialize_with_corresponding_date() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val scenario = launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        scenario.onActivity {
            propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
        }

        onView(allOf(withId(R.id.entry_date_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo(), click())

        val calendar = Calendar.getInstance()
        val entryDate: Date = Utils.fromStringToDate(propertyUpdateFragment.binding.entryDate.text.toString())
        calendar.time = entryDate

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .check(
                matches(
                    object : BoundedMatcher<View, DatePicker>(DatePicker::class.java) {
                        override fun describeTo(description: Description?) {}

                        override fun matchesSafely(item: DatePicker?): Boolean {
                            return ( calendar[Calendar.YEAR] == item?.year && calendar[Calendar.MONTH] == item.month && calendar[Calendar.DAY_OF_MONTH] == item.dayOfMonth)
                        }
                    })
            )
    }

    override fun navigate_to_update_fragment() {
        navigate_to_detail_fragment()
        super.navigate_to_update_fragment()
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}