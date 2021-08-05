package com.openclassrooms.realestatemanager.ui.property.update

import android.view.View
import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.chip.Chip
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.InterestPoint
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyStatus
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INITIAL_ZOOM_LEVEL
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.defaultLocation
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.RxImmediateSchedulerRule
import com.openclassrooms.realestatemanager.util.ToastMatcher
import com.openclassrooms.realestatemanager.util.Utils
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.StringType
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

        fakeProperties = propertiesRepository.findAllProperties().blockingFirst().right!!
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        Completable.fromCallable { if(!Utils.isInternetAvailable()) {
            concatArray(switchAllNetworks(true),
                waitInternetStateChange(true))
                .blockingAwait().let {
                    super.tearDown()
                }
        } else { super.tearDown() }
        }.subscribeOn(SchedulerProvider.io())
    }

    @Test
    fun given_update_when_click_sold_in_alert_dialog_then_sold_date_view_is_shown() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_click_for_rent_in_alert_dialog_then_sold_date_view_is_not_shown() {
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

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
        // Given Detail fragment
        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

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

        ActivityScenario.launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_update_fragment()

        onView(withId(R.id.navigation_update)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_property_is_updated_then_return_on_detail_fragment() {

        ActivityScenario.launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_update_fragment()

        update_property()

        onView(withId(R.id.navigation_update)).perform(click())
        onView(withText(R.string.confirm_save_changes)).perform(click())
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_return_on_detail_after_update_then_detail_property_is_updated_too() {

        ActivityScenario.launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_update_fragment()

        update_property()

        onView(withId(R.id.navigation_update)).perform(click())
        onView(withText(R.string.confirm_save_changes)).perform(click())
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))

        assertThat(obtainDetailFragment().property).isEqualTo(
            BaseFragment.properties.value!!.find { property -> property.id == obtainDetailFragment().property.id })
    }

    @Test
    fun given_update_when_on_back_pressed_then_confirm_dialog_is_shown() {
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_update_fragment()
        update_property()

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_save_changes_dialog_title)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.confirm_save_changes_dialog_message)).inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_on_back_pressed_and_click_confirm_then_return_to_detail_fragment() {
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            INITIAL_ZOOM_LEVEL = 17f
            defaultLocation = leChesnay
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }

        navigate_to_update_fragment()

        update_property()

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_save_changes)).inRoot(isDialog())
            .perform(click())
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_when_has_no_internet_a_message_indicating_property_is_updated_and_saved_only_on_local_storage_is_shown() {

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .blockingAwait().let {

                ActivityScenario.launch(MainActivity::class.java).onActivity {
                    INITIAL_ZOOM_LEVEL = 17f
                    defaultLocation = leChesnay
                    mainActivity = it
                    browseFragment = BrowseFragment()
                    it.setFragment(browseFragment)
                }

                navigate_to_update_fragment()
                update_property()

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
                ActivityScenario.launch(MainActivity::class.java).onActivity {
                    INITIAL_ZOOM_LEVEL = 17f
                    defaultLocation = leChesnay
                    mainActivity = it
                    browseFragment = BrowseFragment()
                    it.setFragment(browseFragment)
                }

                navigate_to_update_fragment()
                update_property()

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
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        onView(withId(R.id.description)).check(matches(withText(containsString(fakeProperties[itemPosition].description))))
        onView(withId(R.id.entry_date)).check(matches(withText(Utils.formatDate(fakeProperties[itemPosition].entryDate))))

        if(fakeProperties[itemPosition].soldDate != null) {
            onView(withId(R.id.sold_date)).check(matches(
                withText(Utils.formatDate(fakeProperties[itemPosition].soldDate) ?:
                testApplication.resources.getString(fakeProperties[itemPosition].status.status))))
        } else {
            onView(withId(R.id.sold_date)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
        onView(withId(R.id.layout_interest_points)).perform(scrollTo())

        InterestPoint.values().forEachIndexed { index, interestPoint ->
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
            onView(withText(interestPoint.place)).check(matches(isDisplayed()))
        }

        onView(withId(R.id.price)).check(matches(withText(containsString(fakeProperties[itemPosition].price.toString()))))
        onView(withId(R.id.type)).check(matches(withText(fakeProperties[itemPosition].type.type)))

        onView(withId(R.id.surface)).check(matches(withText(containsString(fakeProperties[itemPosition].surface.toString()))))
        onView(withId(R.id.rooms)).check(matches(withText(fakeProperties[itemPosition].rooms.toString())))
        onView(withId(R.id.bathrooms)).check(matches(withText(fakeProperties[itemPosition].bathRooms.toString())))
        onView(withId(R.id.bedrooms)).check(matches(withText(fakeProperties[itemPosition].bedRooms.toString())))

        onView(withId(R.id.street)).check(matches(withText(fakeProperties[itemPosition].address!!.street)))
        onView(withId(R.id.city)).check(matches(withText(fakeProperties[itemPosition].address!!.city)))
        onView(withId(R.id.postal_code)).check(matches(withText(fakeProperties[itemPosition].address!!.postalCode)))
        onView(withId(R.id.country)).check(matches(withText(fakeProperties[itemPosition].address!!.country)))
        onView(withId(R.id.state)).check(matches(withText(fakeProperties[itemPosition].address!!.state)))
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_adapter_is_not_null() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }.onFragment {
            propertyUpdateFragment = it
        }
        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter).isNotNull()
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_is_not_empty() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }.onFragment {
            propertyUpdateFragment = it
        }
        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount).isNotEqualTo(0)
    }

    @Test
    fun given_update_when_is_shown_then_photo_recycler_view_count_is_equal_to_property_photo_count() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }.onFragment {
            propertyUpdateFragment = it
        }
        assertThat(propertyUpdateFragment.binding.photosRecyclerView.adapter!!.itemCount)
            .isEqualTo(fakeProperties[itemPosition].photos.size)
    }

    @Test
    fun given_update_when_entry_date_picker_dialog_shown_then_initialize_with_corresponding_date() {

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
        val bundle = bundleOf(FROM to "", PROPERTY_ID to fakeProperties[itemPosition].id)

        launchFragmentInContainer(fragmentArgs = bundle, AppTheme, RESUMED) {
            PropertyUpdateFragment(propertiesViewModelFactory, null)
        }.onFragment {
            propertyUpdateFragment = it
        }

        onView(allOf(withId(R.id.entry_date), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        val entryDate: Date = Utils.fromStringToDate(propertyUpdateFragment.binding.entryDate.text.toString())
        calendar.time = entryDate

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .check(
                matches(
                    object : BoundedMatcher<View, DatePicker>(DatePicker::class.java) {
                        override fun describeTo(description: Description?) {}

                        override fun matchesSafely(item: DatePicker?): Boolean {
                            return ( calendar[Calendar.YEAR] == item?.year && calendar[Calendar.MONTH] == item.month && calendar[Calendar.DAY_OF_MONTH] == item.dayOfMonth);
                        }
                    })
            )
    }

    override fun navigate_to_update_fragment() {
        navigate_to_detail_fragment()
        super.navigate_to_update_fragment()
    }

    private fun update_property() {

        val mockNeat = MockNeat.threadLocal()

        onView(allOf(withId(R.id.description), isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(40).type(StringType.LETTERS).get()))

        onView(allOf(withId(R.id.entry_date), isDisplayed())).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).perform(click())

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            )
        onView(withId(android.R.id.button1)).perform(click())

        InterestPoint.values().forEach {  interestPoint ->
            onView(allOf(withText(testApplication.resources.getString(interestPoint.place)),
                isDisplayed())
            ).perform(click())
        }

        onView(withId(R.id.linearLayout_price_and_type)).perform(scrollTo(), click())
        onView(allOf(withId(R.id.price), isDisplayed()))
            .perform(replaceText(mockNeat.strings().size(6).type(StringType.NUMBERS).get()))

        onView(allOf(withId(R.id.type), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyType.FLAT.type)))
            .perform(click())

        onView(withText(R.string.change_property_type)).perform(click())

        onView(withId(R.id.linearLayout_surface_and_rooms)).perform(scrollTo(), click())

        onView(allOf(withId(R.id.surface), isDisplayed())).perform(
            replaceText(mockNeat.strings().size(3).type(StringType.NUMBERS).get()))
        onView(allOf(withId(R.id.rooms), isDisplayed())).perform(
            replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))

        onView(withId(R.id.linearLayout_bathrooms_and_bedrooms)).perform(scrollTo(), click())

        onView(allOf(withId(R.id.bathrooms), isDisplayed())).perform(
            replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))
        onView(allOf(withId(R.id.bedrooms), isDisplayed())).perform(
            replaceText(mockNeat.strings().size(1).type(StringType.NUMBERS).get()))

        onView(withId(R.id.linearLayout_location)).perform(scrollTo())

        onView(allOf(withId(R.id.street), isDisplayed())).perform(replaceText(mockNeat.strings().size(12).type(StringType.LETTERS).get()))
        onView(allOf(withId(R.id.city), isDisplayed())).perform(replaceText(mockNeat.cities().capitalsEurope().get()))
        onView(allOf(withId(R.id.postal_code), isDisplayed())).perform(replaceText(mockNeat.strings().size(5).type(StringType.NUMBERS).get()))
        onView(allOf(withId(R.id.country), isDisplayed())).perform(replaceText(mockNeat.countries().names().get()))
        onView(allOf(withId(R.id.state), isDisplayed())).perform(replaceText(mockNeat.usStates().get()))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}