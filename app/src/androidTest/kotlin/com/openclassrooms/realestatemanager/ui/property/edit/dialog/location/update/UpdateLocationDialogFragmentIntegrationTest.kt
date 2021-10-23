package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.update

import android.content.res.Configuration
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.android.libraries.places.api.model.Place
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.place.autocomplete.Prediction
import com.openclassrooms.realestatemanager.models.place.detail.PlaceDetails
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.models.property.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.BaseFragmentTests
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment.Companion.EDIT_LOCATION_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import com.openclassrooms.realestatemanager.util.OrientationChangeAction
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class UpdateLocationDialogFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule val espressoIdlingResourceRule = EspressoIdlingResourceRule()
    private lateinit var propertyUpdateFragment: PropertyUpdateFragment

    lateinit var fakePredictions: List<Prediction>
    lateinit var fakePlaceDetails: PlaceDetails

    @Before
    public override fun setUp() {
        super.setUp()
        configure_fake_repository()
        injectTest(testApplication)

        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        fakeProperties = propertiesRepository.findAllProperties().blockingFirst()
        fakeProperties.forEach { property ->
            property.photos = property.photos.toSet().toMutableList()
        }
        itemPosition = (fakeProperties.indices).random()

        BrowseFragment.WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = false

        val predictionsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PREDICTIONS_DATA_FILENAME)
        fakePredictions = Gson().fromJson(predictionsJson, object : TypeToken<List<Prediction>>() {}.type)

        BaseFragment.properties.value = fakeProperties as MutableList<Property>
    }

    @After
    public override fun tearDown() {
        fakeProperties[itemPosition].photos.forEach { photo ->
            val photoFile = File(photo.storageLocalDatabase(testApplication.applicationContext.cacheDir, true))
            if (photoFile.exists()) { photoFile.delete() }
        }
        if (BaseFragment.properties.value != null) { BaseFragment.properties.value!!.clear() }
        (propertiesRepository as DefaultPropertyRepository).cachedProperties.clear()
        super.tearDown()
    }

    @Test
    fun given_update_fragment_when_click_on_update_location_button_then_update_location_dialog_is_shown() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        onView(withId(R.id.edit_location_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_location_dialog_when_rotate_then_alert_dialog_shown_again() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        // Then Update fragment rotate
        val orientation = mainActivity.applicationContext.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationLandscape(mainActivity))
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onView(isRoot())
                .perform(OrientationChangeAction.orientationPortrait(mainActivity))
        }

        onView(withId(R.id.edit_location_dialog_fragment)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_location_dialog_when_is_shown_then_address_details_displayed() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.street)).check(matches(withText(fakeProperties[itemPosition].address.street)))
        onView(withId(R.id.city)).check(matches(withText(fakeProperties[itemPosition].address.city)))
        onView(withId(R.id.postal_code)).check(matches(withText(fakeProperties[itemPosition].address.postalCode)))
        onView(withId(R.id.country)).check(matches(withText(fakeProperties[itemPosition].address.country)))
        onView(withId(R.id.state)).check(matches(withText(fakeProperties[itemPosition].address.state)))
    }

    @Test
    fun given_update_location_dialog_when_is_shown_then_map_is_shown() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        val updateLocationMapFinishLoading = uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        assertThat(updateLocationMapFinishLoading).isTrue()
    }

    @Test
    fun given_update_location_dialog_when_is_shown_then_marker_is_shown_on_map_at_coordinates() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        val marker = uiDevice.findObject(UiSelector().descriptionContains(fakeProperties[itemPosition].address.street))

        assertThat(marker.exists()).isTrue()
    }

    @Test
    fun given_update_location_dialog_when_search_is_launched_then_list_result_is_displayed_and_address_details_are_hidden() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())

        onView(withId(R.id.result_search_location)).check(matches(isDisplayed()))
    }

    @Test
    fun given_update_location_dialog_and_search_is_launched_when_click_on_close_search_button_then_search_view_is_empty_and_focus_is_cleared() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))).perform(
            scrollTo(), click()
        )

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())
        onView(withId(R.id.search_close_btn)).perform(click())

        onView(withId(R.id.search_src_text)).check(matches(withText("")))
        onView(withId(R.id.search_src_text)).check(matches(not(hasFocus())))
    }

    @Test
    fun given_update_location_dialog_and_search_is_launched_when_click_on_us_address_then_us_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())

        val usPlaceDetailsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PLACE_DETAILS_US_DATA_FILENAME)
        fakePlaceDetails = Gson().fromJson(usPlaceDetailsJson, object : TypeToken<PlaceDetails>() {}.type)

        onView(
            withSubstring(
                fakePlaceDetails.addressComponents.single { addressComponent ->
                    addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                }.longName
            )
        ).perform(click())

        onView(withId(R.id.street)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.city)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.LOCALITY.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.postal_code)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(Locale.getDefault()))
                    }.shortName
                )
            )
        )
        onView(withId(R.id.country)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.COUNTRY.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.state)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_update_location_dialog_and_search_is_launched_when_click_on_uk_address_then_uk_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())

        val ukPlaceDetailsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PLACE_DETAILS_UK_DATA_FILENAME)
        fakePlaceDetails = Gson().fromJson(ukPlaceDetailsJson, object : TypeToken<PlaceDetails>() {}.type)

        onView(
            withSubstring(
                fakePlaceDetails.addressComponents.single { addressComponent ->
                    addressComponent.types.contains(Place.Type.POSTAL_TOWN.name.lowercase(Locale.getDefault()))
                }.longName
            )
        ).perform(click())

        onView(withId(R.id.street)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.city)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.POSTAL_TOWN.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.postal_code)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.POSTAL_CODE_PREFIX.name.lowercase(Locale.getDefault()))
                    }.shortName
                )
            )
        )
        onView(withId(R.id.country)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.COUNTRY.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.state)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_update_location_dialog_and_search_is_launched_when_click_on_spanish_address_then_spanish_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())

        val spainPlaceDetailsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PLACE_DETAILS_SPAIN_DATA_FILENAME)
        fakePlaceDetails = Gson().fromJson(spainPlaceDetailsJson, object : TypeToken<PlaceDetails>() {}.type)

        onView(
            withSubstring(
                fakePlaceDetails.addressComponents.single { addressComponent ->
                    addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                }.longName
            )
        ).perform(click())

        onView(withId(R.id.street)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.city)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.LOCALITY.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.postal_code)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(Locale.getDefault()))
                    }.shortName
                )
            )
        )
        onView(withId(R.id.country)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.COUNTRY.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
        onView(withId(R.id.state)).check(
            matches(
                withText(
                    fakePlaceDetails.addressComponents.single { addressComponent ->
                        addressComponent.types.contains(Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(Locale.getDefault()))
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_update_location_dialog_and_location_changed_when_click_on_update_location_button_then_location_in_update_fragment_modified() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
            browseFragment = BrowseFragment()
            it.setFragment(browseFragment)
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        navigate_to_update_fragment()

        propertyUpdateFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text)).perform(typeText(SEARCH_LOCATION_TEXT), pressImeActionButton())

        val spainPlaceDetailsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PLACE_DETAILS_SPAIN_DATA_FILENAME)
        fakePlaceDetails = Gson().fromJson(spainPlaceDetailsJson, object : TypeToken<PlaceDetails>() {}.type)

        onView(
            withSubstring(
                fakePlaceDetails.addressComponents.single { addressComponent ->
                    addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                }.longName
            )
        ).perform(click())

        val tmpAddress: Address = propertyUpdateFragment.updateLocationAlertDialog.tmpAddress

        onView(withText(R.string.update_location)).perform(click())

        onView(allOf(withId(R.id.street), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo()).check(matches(withText(tmpAddress.street)))
        onView(allOf(withId(R.id.city), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo()).check(matches(withText(tmpAddress.city)))
        onView(allOf(withId(R.id.postal_code), withEffectiveVisibility(VISIBLE)))
            .check(matches(withText(tmpAddress.postalCode)))
        onView(allOf(withId(R.id.country), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo()).check(matches(withText(tmpAddress.country)))
        onView(allOf(withId(R.id.state), withEffectiveVisibility(VISIBLE)))
            .check(matches(withText(tmpAddress.state)))
    }

    override fun navigate_to_update_fragment() {
        navigate_to_detail_fragment()
        super.navigate_to_update_fragment()
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    companion object {
        const val SEARCH_LOCATION_TEXT = "france"
    }
}
