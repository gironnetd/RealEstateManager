package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.add

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.bottomnavigation.BottomNavigationItemView
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
import com.openclassrooms.realestatemanager.ui.fragments.MainNavHostFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment.Companion.EDIT_LOCATION_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.EspressoIdlingResourceRule
import com.openclassrooms.realestatemanager.util.OrientationChangeAction
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class AddLocationDialogFragmentIntegrationTest : BaseFragmentTests() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val espressoIdlingResourceRule = EspressoIdlingResourceRule()
    private lateinit var propertyCreateFragment: PropertyCreateFragment

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

        fakeProperties[itemPosition].photos.forEach { photo ->
            val photoFile = File(photo.storageLocalDatabase(testApplication.applicationContext.cacheDir, true))

            if (!photoFile.exists()) {
                val defaultImage = testApplication.resources.getDrawable(R.drawable.default_image, null)

                val outputStream = FileOutputStream(photoFile, true)

                (defaultImage as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            }
        }

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
    fun given_create_fragment_when_click_on_create_location_button_then_create_location_dialog_is_shown() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        onView(withId(R.id.edit_location_dialog_fragment))
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_create_location_dialog_when_rotate_then_alert_dialog_shown_again() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

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

        onView(withId(R.id.edit_location_dialog_fragment))
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_create_location_dialog_when_is_shown_then_address_details_displayed() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        val none = testApplication.resources.getString(R.string.none)

        onView(withId(R.id.street)).inRoot(isDialog()).check(matches(withText(none)))
        onView(withId(R.id.city)).inRoot(isDialog()).check(matches(withText(none)))
        onView(withId(R.id.postal_code)).inRoot(isDialog()).check(matches(withText(none)))
        onView(withId(R.id.country)).inRoot(isDialog()).check(matches(withText(none)))
        onView(withId(R.id.state)).inRoot(isDialog()).check(matches(withText(none)))
    }

    @Test
    fun given_create_location_dialog_when_is_shown_then_map_is_shown() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        val addLocationMapFinishLoading = uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        assertThat(addLocationMapFinishLoading).isTrue()
    }

    @Test
    fun given_create_location_dialog_when_search_is_launched_then_list_result_is_displayed_and_address_details_are_hidden() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())

        onView(withId(R.id.result_search_location))
            .check(matches(isDisplayed()))
    }

    @Test
    fun given_create_location_dialog_and_search_is_launched_when_click_on_close_search_button_then_search_view_is_empty_and_focus_is_cleared() {

        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())
        onView(withId(R.id.search_close_btn)).perform(click())

        onView(withId(R.id.search_src_text))
            .check(matches(withText("")))
        onView(withId(R.id.search_src_text))
            .check(matches(CoreMatchers.not(hasFocus())))
    }

    @Test
    fun given_create_location_dialog_and_search_is_launched_when_click_on_us_address_then_us_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE)))
            .perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())

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
                        addressComponent.types.contains(
                            Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                Locale.getDefault()
                            )
                        )
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
                        addressComponent.types.contains(
                            Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                Locale.getDefault()
                            )
                        )
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_create_location_dialog_and_search_is_launched_when_click_on_uk_address_then_uk_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())

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
                        addressComponent.types.contains(
                            Place.Type.POSTAL_CODE_PREFIX.name.lowercase(
                                Locale.getDefault()
                            )
                        )
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
                        addressComponent.types.contains(
                            Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                Locale.getDefault()
                            )
                        )
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_create_location_dialog_and_search_is_launched_when_click_on_spanish_address_then_spanish_address_is_correctly_display_on_details_party() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())

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
                        addressComponent.types.contains(
                            Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                Locale.getDefault()
                            )
                        )
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
                        addressComponent.types.contains(
                            Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                Locale.getDefault()
                            )
                        )
                    }.longName
                )
            )
        )
    }

    @Test
    fun given_create_location_dialog_and_location_changed_when_click_on_create_location_button_then_location_in_create_fragment_modified() {
        launch(MainActivity::class.java).onActivity {
            mainActivity = it
        }
        isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

        onView(allOf(withId(R.id.navigation_create), isAssignableFrom(BottomNavigationItemView::class.java))).perform(click())

        propertyCreateFragment = (mainActivity.supportFragmentManager.primaryNavigationFragment as MainNavHostFragment).childFragmentManager.primaryNavigationFragment as PropertyCreateFragment

        onView(allOf(withId(R.id.map_view_button), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo(), click())

        uiDevice.wait(
            Until.hasObject(By.desc(EDIT_LOCATION_MAP_FINISH_LOADING)),
            20000
        )

        onView(withId(R.id.search_src_text))
            .perform(ViewActions.typeText(SEARCH_LOCATION_TEXT), ViewActions.pressImeActionButton())

        val spainPlaceDetailsJson = jsonUtil.readJSONFromAsset(ConstantsTest.PLACE_DETAILS_SPAIN_DATA_FILENAME)
        fakePlaceDetails = Gson().fromJson(spainPlaceDetailsJson, object : TypeToken<PlaceDetails>() {}.type)

        onView(
            withSubstring(
                fakePlaceDetails.addressComponents.single { addressComponent ->
                    addressComponent.types.contains(Place.Type.ROUTE.name.lowercase(Locale.getDefault()))
                }.longName
            )
        ).perform(click())

        val tmpAddress: Address = propertyCreateFragment.addLocationAlertDialog.tmpAddress

        onView(withText(R.string.create_location)).perform(click())

        onView(allOf(withId(R.id.street), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo()).check(
            matches(
                withText(
                    tmpAddress.street
                )
            )
        )
        onView(allOf(withId(R.id.city), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo()).check(
            matches(
                withText(
                    tmpAddress.city
                )
            )
        )
        onView(allOf(withId(R.id.postal_code), withEffectiveVisibility(VISIBLE))
        ).check(matches(withText(tmpAddress.postalCode)))

        onView(allOf(withId(R.id.country), withEffectiveVisibility(VISIBLE))
        ).perform(scrollTo()).check(
            matches(
                withText(
                    tmpAddress.country
                )
            )
        )
        onView(allOf(withId(R.id.state), withEffectiveVisibility(VISIBLE))
        ).check(matches(withText(tmpAddress.state)))
    }

    override fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    companion object {
        const val SEARCH_LOCATION_TEXT = "france"
    }
}