package com.openclassrooms.realestatemanager.ui.property.setting

import android.graphics.drawable.VectorDrawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.internal.CheckableImageButton
import com.google.android.material.internal.NavigationMenuItemView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.BrowseListFragment
import com.openclassrooms.realestatemanager.ui.property.search.MainSearchFragment
import com.openclassrooms.realestatemanager.ui.property.setting.Currency.DOLLARS
import com.openclassrooms.realestatemanager.ui.property.setting.Currency.EUROS
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.list.ListAdapter
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.ConnectivityUtil
import com.openclassrooms.realestatemanager.util.Constants.DEFAULT_CURRENCY
import com.openclassrooms.realestatemanager.util.RxImmediateSchedulerRule
import com.openclassrooms.realestatemanager.util.Utils
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyCurrencySettingIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        BaseFragment.properties.value = fakeProperties.toMutableList()
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false
    }

    @After
    public override fun tearDown() {
        if (BaseFragment.properties.value != null) { BaseFragment.properties.value!!.clear() }

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

    @Test
    //@Order(1)
    fun given_navigation_view_when_euros_is_default_currency_then_euros_choice_button_is_checked_and_indicated_as_default_currency() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.euros_choice)))
                .check(matches(isChecked()))
    }

    @Test
    //@Order(2)
    fun given_navigation_view_when_euros_is_default_currency_and_dollar_choice_button_is_pressed_then_dollar_choice_button_is_checked_and_indicated_as_default_currency() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.euros_choice)))
                .check(matches(isChecked()))

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.dollars_choice)))
                .check(matches(isChecked()))
    }

    @Test
    //@Order(3)
    fun given_navigation_view_when_dollar_is_default_currency_then_dollars_choice_button_is_checked_and_indicated_as_default_currency() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.dollars_choice)))
                .check(matches(isChecked()))
    }

    @Test
    //@Order(4)
    fun given_navigation_view_when_dollar_is_default_currency_and_euros_choice_button_is_pressed_then_euros_choice_button_is_checked_and_indicated_as_default_currency() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.dollars_choice)))
                .check(matches(isChecked()))

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        onView(allOf(isAssignableFrom(MaterialRadioButton::class.java), withId(R.id.euros_choice)))
                .check(matches(isChecked()))
    }

    @Test
    //@Order(5)
    fun given_list_fragment_when_euros_is_default_currency_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        val propertiesRecyclerView = (((mainActivity as MainActivity).
        navHostFragment.childFragmentManager.primaryNavigationFragment as BrowseFragment).master as BrowseListFragment).binding.propertiesRecyclerView

        fakeProperties.forEachIndexed { index, _ ->
            var viewHolder = propertiesRecyclerView
                    .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder

            if(viewHolder == null) {
                onView(withId(propertiesRecyclerView.id)).perform(RecyclerViewActions.scrollToPosition<ListAdapter.PropertyViewHolder>(index))
                viewHolder = propertiesRecyclerView
                        .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder
            }

            assertTrue(viewHolder!!.price.text.startsWith(testApplication.resources.getString(R.string.euros_symbol)))
        }
    }

    @Test
    //@Order(6)
    fun given_list_fragment_and_euros_is_default_currency_when_dollars_is_selected_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        val propertiesRecyclerView = (((mainActivity as MainActivity).
        navHostFragment.childFragmentManager.primaryNavigationFragment as BrowseFragment).master as BrowseListFragment).binding.propertiesRecyclerView

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        fakeProperties.forEachIndexed { index, _ ->
            var viewHolder = propertiesRecyclerView
                    .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder

            if(viewHolder == null) {
                onView(withId(propertiesRecyclerView.id)).perform(RecyclerViewActions.scrollToPosition<ListAdapter.PropertyViewHolder>(index))
                viewHolder = propertiesRecyclerView
                        .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder
            }

            assertTrue(viewHolder!!.price.text.startsWith(testApplication.resources.getString(R.string.dollars_symbol)))
        }
    }

    @Test
    //@Order(7)
    fun given_list_fragment_when_dollars_is_default_currency_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        val propertiesRecyclerView = (((mainActivity as MainActivity).
        navHostFragment.childFragmentManager.primaryNavigationFragment as BrowseFragment).master as BrowseListFragment).binding.propertiesRecyclerView

        fakeProperties.forEachIndexed { index, _ ->
            var viewHolder = propertiesRecyclerView
                    .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder

            if(viewHolder == null) {
                onView(withId(propertiesRecyclerView.id)).perform(RecyclerViewActions.scrollToPosition<ListAdapter.PropertyViewHolder>(index))
                viewHolder = propertiesRecyclerView
                        .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder
            }

            assertTrue(viewHolder!!.price.text.startsWith(testApplication.resources.getString(R.string.dollars_symbol)))
        }
    }

    @Test
    //@Order(8)
    fun given_list_fragment_and_dollars_is_default_currency_when_euros_is_selected_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        val propertiesRecyclerView = (((mainActivity as MainActivity).
        navHostFragment.childFragmentManager.primaryNavigationFragment as BrowseFragment).master as BrowseListFragment).binding.propertiesRecyclerView

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        fakeProperties.forEachIndexed { index, _ ->
            var viewHolder = propertiesRecyclerView
                    .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder

            if(viewHolder == null) {
                onView(withId(propertiesRecyclerView.id)).perform(RecyclerViewActions.scrollToPosition<ListAdapter.PropertyViewHolder>(index))
                viewHolder = propertiesRecyclerView
                        .findViewHolderForAdapterPosition(index) as? ListAdapter.PropertyViewHolder
            }

            assertTrue(viewHolder!!.price.text.startsWith(testApplication.resources.getString(R.string.euros_symbol)))
        }
    }

    @Test
    //@Order(9)
    fun given_detail_fragment_when_euros_is_default_currency_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_detail_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(10)
    fun given_detail_fragment_and_euros_is_default_currency_when_dollars_is_selected_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_detail_fragment()

        click_on_navigate_up_button()

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        navigate_to_detail_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(11)
    fun given_detail_fragment_when_dollars_is_default_currency_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_detail_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(12)
    fun given_detail_fragment_and_dollars_is_default_currency_when_euros_is_selected_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_detail_fragment()

        click_on_navigate_up_button()

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        navigate_to_detail_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(13)
    fun given_update_fragment_when_euros_is_default_currency_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(14)
    fun given_update_fragment_and_euros_is_default_currency_when_dollars_is_selected_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        click_on_navigate_up_button()
        click_on_navigate_up_button()

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(15)
    fun given_update_fragment_when_dollars_is_default_currency_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(16)
    fun given_update_fragment_and_dollars_is_default_currency_when_euros_is_selected_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        click_on_navigate_up_button()
        click_on_navigate_up_button()

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(17)
    fun given_create_fragment_when_euros_is_default_currency_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }

        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(18)
    fun given_create_fragment_and_euros_is_default_currency_when_dollars_is_selected_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }

        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(19)
    fun given_create_fragment_when_dollars_is_default_currency_then_price_is_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(20)
    fun given_create_fragment_and_dollars_is_default_currency_when_euros_is_selected_then_price_is_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }

        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isDisplayed())).perform(click())

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(allOf(withId(R.id.price_text_input_layout), withEffectiveVisibility(VISIBLE))).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }


    @Test
    //@Order(21)
    fun given_search_fragment_when_euros_is_default_currency_then_min_and_max_price_are_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(allOf(withId(R.id.min_price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.min_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }

        onView(withId(R.id.max_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(22)
    fun given_search_fragment_and_euros_is_default_currency_when_dollars_is_selected_then_min_and_max_price_are_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.dollars_choice)))
                .perform(click())

        onView(allOf(withId(R.id.min_price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.min_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }

        onView(withId(R.id.max_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(23)
    fun given_search_fragment_when_dollars_is_default_currency_then_min_and_max_price_are_indicated_in_dollars() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(allOf(withId(R.id.min_price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.min_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }

        onView(withId(R.id.max_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_dollar_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
    }

    @Test
    //@Order(24)
    fun given_search_fragment_and_dollars_is_default_currency_when_euros_is_selected_then_min_and_max_price_are_indicated_in_euros() {
        // Given Main activity is launched and Navigation View is opened
        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).commit()

        activityScenario = ActivityScenario.launch(MainActivity::class.java).onActivity {
            mainActivity = it
            val mainSearchFragment = MainSearchFragment()
            it.setFragment(mainSearchFragment)
        }

        onView(allOf(withContentDescription(
                activityScenario.get_toolbar_navigation_content_description()), isDisplayed()))
                .perform(click())

        onView(allOf(isAssignableFrom(NavigationMenuItemView::class.java), withId(R.id.euros_choice)))
                .perform(click())

        onView(allOf(withId(R.id.min_price_text_input_layout), withEffectiveVisibility(VISIBLE))).perform(scrollTo())

        onView(withId(R.id.min_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }

        onView(withId(R.id.max_price_text_input_layout)).check { view, _ ->
            val endIcon = view.findViewById<CheckableImageButton>(R.id.text_input_end_icon)

            assertThat(endIcon.drawable).isInstanceOf(VectorDrawable::class.java)
            val actualEndIcon = endIcon.drawable.toBitmap()
            val expectedEndIcon = testApplication.resources.getDrawable(R.drawable.ic_baseline_euro_24, null).toBitmap()
            assertThat(BitmapUtil.sameAs(actualEndIcon, expectedEndIcon)).isTrue()
        }
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