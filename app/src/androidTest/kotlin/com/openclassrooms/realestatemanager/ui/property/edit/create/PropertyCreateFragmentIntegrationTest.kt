package com.openclassrooms.realestatemanager.ui.property.edit.create

import android.view.View
import android.widget.DatePicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.property.PropertyStatus
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.util.EnterPropertyUtil.update_property
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.Constants.TIMEOUT_INTERNET_CONNECTION
import com.openclassrooms.realestatemanager.util.Order
import com.openclassrooms.realestatemanager.util.OrderedRunner
import com.openclassrooms.realestatemanager.util.RxImmediateSchedulerRule
import com.openclassrooms.realestatemanager.util.Utils
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import io.reactivex.Single
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(OrderedRunner::class)
@MediumTest
class PropertyCreateFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private val expectedAppName by lazy { mainActivity.getString(R.string.app_name) }
    private val expectedTitle by lazy { mainActivity.getString(R.string.notification_title) }
    private val propertyFullyCreated by lazy { mainActivity.getString(R.string.property_create_totally) }
    private val propertyLocallyCreated by lazy { mainActivity.getString(R.string.property_create_locally) }
    private val clearAllNotificationRes = "com.android.systemui:id/dismiss_text"

    private val timeout = 3_000L

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

    @Order(1)
    @Test
    fun given_create_when_has_no_internet_a_message_indicating_property_is_created_only_on_local_storage_is_shown() {
        concatArray(switchAllNetworks(false),
            waitInternetStateChange(false))
            .blockingAwait().let {
                // Given Create fragment
                launch(MainActivity::class.java).onActivity { mainActivity = it }

                onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())
                update_property(testApplication = testApplication)

                onView(allOf(withText(R.string.create), isDisplayed())).perform(click())
                onView(withText(R.string.confirm_create_changes)).perform(click())

                uiDevice.openNotification()
                uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeout)

                val title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
                val text: UiObject2 = uiDevice.findObject(By.textStartsWith(propertyLocallyCreated))

                assertEquals(expectedTitle, title.text)
                assertEquals(propertyLocallyCreated, text.text)

                clearAllNotifications()

                concatArray(switchAllNetworks(true),
                    waitInternetStateChange(true))
                    .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Order(2)
    @Test
    fun given_create_when_click_sold_in_alert_dialog_then_sold_date_view_is_shown() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status))).perform(
            click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))
    }

    @Order(3)
    @Test
    fun given_create_when_click_for_rent_in_alert_dialog_then_sold_date_view_is_not_shown() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.FOR_RENT.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(ViewAssertions.doesNotExist())
    }

    @Order(4)
    @Test
    fun given_create_when_click_on_in_sale_in_alert_dialog_then_sold_date_view_is_not_shown() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.SOLD.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.status), isDisplayed())).perform(click())

        onView(withText(testApplication.resources.getString(PropertyStatus.IN_SALE.status)))
            .perform(click())

        onView(withText(R.string.change_property_status)).perform(click())

        onView(allOf(withId(R.id.sold_date), isDisplayed())).check(ViewAssertions.doesNotExist())
    }

    @Order(5)
    @Test
    fun given_create_when_navigate_on_create_fragment_then_create_menu_item_is_shown() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withText(R.string.create), isDisplayed())).check(matches(isDisplayed()))
    }

    @Order(6)
    @Test
    fun given_create_when_property_is_create_then_return_on_browse_fragment() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        update_property(testApplication = testApplication)

        onView(allOf(withText(R.string.create), isDisplayed())).perform(click())
        onView(withText(R.string.confirm_create_changes)).perform(click())
        onView(withId(R.id.browse_fragment)).check(matches(isDisplayed()))
        clearAllNotifications()
    }

    @Order(7)
    @Test
    fun given_create_when_on_back_pressed_then_confirm_dialog_is_shown() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        update_property(testApplication = testApplication)

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_create_changes_dialog_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText(R.string.confirm_create_changes_dialog_message))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Order(8)
    @Test
    fun given_create_when_on_back_pressed_and_click_confirm_then_return_to_browse_fragment() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        update_property(testApplication = testApplication)

        onView(isRoot()).perform(pressBack())

        onView(withText(R.string.confirm_create_changes))
            .inRoot(isDialog())
            .perform(click())
        onView(withId(R.id.browse_fragment))
            .check(matches(isDisplayed()))
        clearAllNotifications()
    }

    @Order(9)
    @Test
    @Suppress("UnstableApiUsage")
    fun given_create_when_has_no_internet_and_property_created_when_has_internet_then_a_message_indicating_property_is_totally_created_is_shown() {
        concatArray(switchAllNetworks(false),
            waitInternetStateChange(false))
            .blockingAwait().let {
                // Given Create fragment
                launch(MainActivity::class.java).onActivity {
                    mainActivity = it
                }

                onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())
                update_property(testApplication = testApplication)

                onView(allOf(withText(R.string.create), isDisplayed())).perform(click())
                onView(withText(R.string.confirm_create_changes)).perform(click())

                uiDevice.openNotification()
                uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeout)

                var title: UiObject2 = uiDevice.findObject(By.text(expectedTitle))
                var text: UiObject2 = uiDevice.findObject(By.textStartsWith(propertyLocallyCreated))

                assertEquals(expectedTitle, title.text)
                assertEquals(propertyLocallyCreated, text.text)
                clearAllNotifications()

                concatArray(switchAllNetworks(true),
                    waitInternetStateChange(true))
                    .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                    .blockingAwait().let {
                        Completable.fromAction {
                            uiDevice.openNotification()
                            uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeout)

                            title = uiDevice.findObject(By.text(expectedTitle))
                            text = uiDevice.findObject(By.textStartsWith(propertyFullyCreated))

                            assertEquals(expectedTitle, title.text)
                            assertEquals(propertyFullyCreated, text.text)

                            clearAllNotifications()
                        }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2),
                            TimeUnit.MILLISECONDS).blockingAwait()
                    }
            }
    }

    @Order(10)
    @Test
    fun given_create_when_entry_date_picker_dialog_shown_then_initialize_with_corresponding_date() {
        // Given Create fragment
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.entry_date), isDisplayed())).perform(click())
        val calendar = Calendar.getInstance()

        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .check(
                matches(
                    object : BoundedMatcher<View, DatePicker>(DatePicker::class.java) {
                        override fun describeTo(description: Description?) {}

                        override fun matchesSafely(item: DatePicker?): Boolean {
                            return (calendar[Calendar.YEAR] == item?.year && calendar[Calendar.MONTH] == item.month && calendar[Calendar.DAY_OF_MONTH] == item.dayOfMonth)
                        }
                    })
            )
    }

    private fun clearAllNotifications() {
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(expectedAppName)), timeout)
        val clearAll: UiObject2 = uiDevice.findObject(By.res(clearAllNotificationRes))
        clearAll.click()
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}