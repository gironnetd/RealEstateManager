package com.openclassrooms.realestatemanager.util

import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.*
import com.google.common.truth.Truth.assertThat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment.Companion.DETAIL_MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.shared.list.ListAdapter
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.INFO_WINDOW_SHOWN
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.MAP_FINISH_LOADING
import org.hamcrest.core.AllOf.allOf

object NavigationHelper {

    fun navigate_to_detail_fragment_in_normal_mode(itemPosition: Int ) {
        with(onView(withId(R.id.properties_recycler_view))) {
            check(matches(isDisplayed()))
            perform(RecyclerViewActions.scrollToPosition<ListAdapter.PropertyViewHolder>(itemPosition))
            perform(RecyclerViewActions.actionOnItemAtPosition<ListAdapter.PropertyViewHolder>(
                    itemPosition, click()))
        }
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
    }

    fun navigate_to_detail_fragment_in_master_detail_mode(testApplication: TestBaseApplication, uiDevice: UiDevice, mainActivity : FragmentActivity,
        browseFragment: BrowseFragment, itemPosition: Int, fakeProperties: List<Property>) {
        try {
            wait_until_map_is_finished_loading(uiDevice)

            val marker = uiDevice.findObject(UiSelector().descriptionContains(fakeProperties[itemPosition].address.street))
            if(marker.exists()) {
                marker.click()
                uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOWN)), 30000)

                val mapFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as BaseMapFragment
                val listFragment = browseFragment.master

                val displayManager = testApplication.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                val size = Point()
                display.getRealSize(size)
                val screenHeight = size.y
                val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                val y = (screenHeight * 0.40).toInt()

                // Click on the InfoWindow, using UIAutomator
                uiDevice.click(x, y)
                uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                    mainActivity.resources.getResourceEntryName(R.id.edit_fragment))), 30000)

                wait_until_detail_map_is_finished_loading(uiDevice)
            }
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }
    }

    fun navigate_to_update_fragment(uiDevice: UiDevice, mainActivity : FragmentActivity) {
        onView(allOf(withId(R.id.edit_fragment), isDisplayed())).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_edit), isDisplayed())).perform(click())
        uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
            mainActivity.resources.getResourceEntryName(R.id.edit_fragment))), 10000)
    }

    fun wait_until_map_is_finished_loading(uiDevice: UiDevice) {
        var isMapFinishLoading =
            uiDevice.findObject(UiSelector()
                .descriptionContains(MAP_FINISH_LOADING)).exists()
                    || uiDevice.findObject(UiSelector()
                .descriptionContains(INFO_WINDOW_SHOWN)).exists()

        if(!isMapFinishLoading) {
            isMapFinishLoading = uiDevice.wait(Until.hasObject(By.desc(MAP_FINISH_LOADING)),
                50000)
            assertThat(isMapFinishLoading).isTrue()
        }
    }

    fun wait_until_detail_map_is_finished_loading(uiDevice: UiDevice) {
        if(!uiDevice.findObject(UiSelector().descriptionContains(DETAIL_MAP_FINISH_LOADING)).exists()) {
            onView(withId(R.id.map_detail_fragment)).perform(scrollTo())

            uiDevice.wait(Until.hasObject(By.desc(DETAIL_MAP_FINISH_LOADING)), 50000)
            //assertThat(isDetailMapFinishLoading).isTrue()
        }
    }
}