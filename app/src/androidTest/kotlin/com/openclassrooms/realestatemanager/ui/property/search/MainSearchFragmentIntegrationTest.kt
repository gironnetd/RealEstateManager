package com.openclassrooms.realestatemanager.ui.property.search

import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.InterestPoint
import com.openclassrooms.realestatemanager.models.property.PropertyStatus
import com.openclassrooms.realestatemanager.models.property.PropertyType
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.*
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(OrderedRunner::class)
@MediumTest
class MainSearchFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()

        BaseFragment.properties.value = fakeProperties.toMutableList()
        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        if (BaseFragment.properties.value != null) {
            BaseFragment.properties.value!!.clear()
        }

        if(BrowseResultFragment.searchedProperties.value != null) {
            BrowseResultFragment.searchedProperties.value!!.clear()
        }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        Single.fromCallable { Utils.isInternetAvailable() }
            .doOnSuccess { isInternetAvailable ->
                if (!isInternetAvailable) {
                    Completable.concatArray(
                        ConnectivityUtil.switchAllNetworks(true),
                        ConnectivityUtil.waitInternetStateChange(true)
                    )
                        .blockingAwait().let {
                            super.tearDown()
                        }
                } else { super.tearDown() }
            }.subscribeOn(SchedulerProvider.io()).blockingGet()
    }

    @Order(1)
    @Test
    fun given_main_search_fragment_when_click_on_in_sale_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On In Sale Button, Entry Date and Enter value
        onView(withId(R.id.in_sale_radio_button)).perform(click())
        onView(
            allOf(
                withId(R.id.in_sale_text_edit),
                isDisplayed()
            )
        ).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == PropertyStatus.IN_SALE }.random().entryDate!!.time

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!
                .all { property -> property.status == PropertyStatus.IN_SALE && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(2)
    @Test
    fun given_main_search_fragment_when_click_on_for_rent_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On For Rent Button, Entry Date and Enter value
        onView(withId(R.id.for_rent_radio_button))
            .perform(click())
        onView(
            allOf(
                withId(R.id.for_rent_text_edit),
                isDisplayed()
            )
        ).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == PropertyStatus.FOR_RENT }.random().entryDate!!.time

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!
                .all { property -> property.status == PropertyStatus.FOR_RENT && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(3)
    @Test
    fun given_main_search_fragment_when_click_on_sold_button_and_entry_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Sold Button, Entry Date and Enter value
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(
            allOf(
                withId(R.id.sold_entry_date_text_edit),
                isDisplayed()
            )
        ).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == PropertyStatus.SOLD }.random().entryDate!!.time

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!
                .all { property -> property.status == PropertyStatus.SOLD && property.entryDate!!.time >= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(4)
    @Test
    fun given_main_search_fragment_when_click_on_sold_button_and_sold_date_updated_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Sold Button, Sold Date and Enter value
        onView(withId(R.id.sold_radio_button)).perform(click())
        onView(
            allOf(
                withId(R.id.sold_date_text_edit),
                isDisplayed()
            )
        ).perform(click())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fakeProperties.filter { property -> property.status == PropertyStatus.SOLD }.random().soldDate!!.time

        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
            )
        onView(withId(android.R.id.button1)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!
                .all { property -> property.status == PropertyStatus.SOLD && property.soldDate!!.time <= calendar.timeInMillis }
            ).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(5)
    @Test
    fun given_main_search_fragment_when_click_on_search_button_then_result_fragment_is_shown() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())
        onView(withId(R.id.browse_fragment))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Order(6)
    @Test
    fun given_main_search_fragment_when_click_on_flat_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Flat CheckBox
        onView(withId(R.id.flat_checkbox)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.type == PropertyType.FLAT })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(7)
    @Test
    fun given_main_search_fragment_when_click_on_townhouse_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Townhouse CheckBox
        onView(withId(R.id.townhouse_checkbox)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.type == PropertyType.TOWNHOUSE })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(8)
    @Test
    fun given_main_search_fragment_when_click_on_penthouse_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Penthouse CheckBox
        onView(withId(R.id.penthouse_checkbox)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.type == PropertyType.PENTHOUSE })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(9)
    @Test
    fun given_main_search_fragment_when_click_on_house_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On House CheckBox
        onView(withId(R.id.house_checkbox)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.type == PropertyType.HOUSE })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(10)
    @Test
    fun given_main_search_fragment_when_click_on_duplex_checkbox_and_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Duplex CheckBox
        onView(withId(R.id.duplex_checkbox)).perform(click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.type == PropertyType.DUPLEX })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(11)
    @Test
    fun given_main_search_fragment_when_click_on_min_price_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Price and Enter value
        onView(withId(R.id.min_price))
            .perform(ViewActions.scrollTo(), click())
        val minPrice = fakeProperties.random().price
        onView(withId(R.id.min_price))
            .perform(ViewActions.replaceText(minPrice.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.price >= minPrice })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(12)
    @Test
    fun given_main_search_fragment_when_click_on_max_price_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Price and Enter value
        onView(withId(R.id.max_price))
            .perform(ViewActions.scrollTo(), click())
        val maxPrice = fakeProperties.random().price
        onView(withId(R.id.max_price))
            .perform(ViewActions.replaceText(maxPrice.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.price <= maxPrice })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(13)
    @Test
    fun given_main_search_fragment_when_click_on_min_surface_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Surface and Enter value
        onView(withId(R.id.min_surface))
            .perform(ViewActions.scrollTo(), click())
        val minSurface = fakeProperties.random().surface
        onView(withId(R.id.min_surface))
            .perform(ViewActions.replaceText(minSurface.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.surface >= minSurface })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(14)
    @Test
    fun given_main_search_fragment_when_click_on_max_surface_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Surface and Enter value
        onView(withId(R.id.max_surface))
            .perform(ViewActions.scrollTo(), click())
        val maxSurface = fakeProperties.random().surface
        onView(withId(R.id.max_surface))
            .perform(ViewActions.replaceText(maxSurface.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.surface <= maxSurface })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(15)
    @Test
    fun given_main_search_fragment_when_click_on_min_rooms_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Min Rooms and Enter value
        onView(withId(R.id.min_rooms))
            .perform(ViewActions.scrollTo(), click())
        val minRooms = fakeProperties.random().rooms
        onView(withId(R.id.min_rooms))
            .perform(ViewActions.replaceText(minRooms.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.rooms >= minRooms })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(16)
    @Test
    fun given_main_search_fragment_when_click_on_max_rooms_button_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Max Rooms and Enter value
        onView(withId(R.id.max_rooms))
            .perform(ViewActions.scrollTo(), click())
        val maxRooms = fakeProperties.random().rooms
        onView(withId(R.id.max_rooms))
            .perform(ViewActions.replaceText(maxRooms.toString()))

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property -> property.rooms <= maxRooms })
                .isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(17)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_school_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby School
        onView(withText(testApplication.resources.getString(R.string.interest_point_school)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.SCHOOL
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(18)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_playground_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Playground
        onView(withText(testApplication.resources.getString(R.string.interest_point_playground)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.PLAYGROUND
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(19)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_shop_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Shop
        onView(withText(testApplication.resources.getString(R.string.interest_point_shop)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.SHOP
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(20)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_buses_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Buses
        onView(withText(testApplication.resources.getString(R.string.interest_point_buses)))
            .perform(
                ViewActions.scrollTo(), click()
            )

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.BUSES
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(21)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_subway_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Subway
        onView(withText(testApplication.resources.getString(R.string.interest_point_subway)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.SUBWAY
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(22)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_park_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Park
        onView(withText(testApplication.resources.getString(R.string.interest_point_park)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.PARK
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(23)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_hospital_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Hospital
        onView(withText(testApplication.resources.getString(R.string.interest_point_hospital)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.HOSPITAL
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(24)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_restaurants_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Restaurants
        onView(withText(testApplication.resources.getString(R.string.interest_point_restaurants)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.RESTAURANTS
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    @Order(48)
    @Test
    fun given_main_search_fragment_when_click_on_nearby_gas_stations_and_click_on_search_button_then_properties_are_correctly_filtered() {
        // Given Main Search fragment
        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        // Click On Nearby Gas Stations
        onView(withText(testApplication.resources.getString(R.string.interest_point_gas_stations)))
            .perform(ViewActions.scrollTo(), click())

        // Click On Search Button
        onView(
            allOf(
                withId(R.id.menu_item_container),
                isDisplayed()
            )
        ).perform(click())

        // Then Properties are filtered
        BrowseResultFragment.searchedProperties.value?.let {
            assertThat(BrowseResultFragment.searchedProperties.value!!.all { property ->
                property.interestPoints.contains(
                    InterestPoint.GAS_STATIONS
                )
            }).isTrue()
        } ?: onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.no_property_found)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}