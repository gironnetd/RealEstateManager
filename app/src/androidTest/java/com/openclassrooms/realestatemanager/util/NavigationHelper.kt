package com.openclassrooms.realestatemanager.util

import android.graphics.Point
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
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListAdapter
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.INFO_WINDOW_SHOWN
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.MAP_FINISH_LOADING
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment.Companion.DETAIL_MAP_FINISH_LOADING
import org.hamcrest.core.AllOf.allOf

object NavigationHelper {

    fun navigate_to_detail_fragment_in_normal_mode(itemPosition: Int ) {
        with(onView(withId(R.id.recycler_view))) {
            check(matches(isDisplayed()))
            perform(RecyclerViewActions.actionOnItemAtPosition<ListAdapter.PropertyViewHolder>(
                        itemPosition, click()))
        }
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
    }

    fun navigate_to_detail_fragment_in_master_detail_mode(uiDevice: UiDevice, mainActivity : FragmentActivity,
        browseFragment: BrowseFragment, itemPosition: Int, fakeProperties: List<Property>) {
        try {
            wait_until_map_is_finished_loading(uiDevice)

            val marker = uiDevice.findObject(UiSelector().descriptionContains(fakeProperties[itemPosition].address!!.street))
            if(marker.exists()) {
                marker.click()
                uiDevice.wait(Until.hasObject(By.desc(INFO_WINDOW_SHOWN)), 30000)

                val mapFragment = browseFragment.detail.childFragmentManager.primaryNavigationFragment as MapFragment
                val listFragment = browseFragment.master

                val display = mainActivity.windowManager.defaultDisplay
                val size = Point()
                display.getRealSize(size)
                val screenHeight = size.y
                val x = listFragment.view!!.width + mapFragment.view!!.width / 2
                val y = (screenHeight * 0.40).toInt()

                // Click on the InfoWindow, using UIAutomator
                uiDevice.click(x, y)
                uiDevice.wait(Until.hasObject(By.res(mainActivity.packageName,
                    mainActivity.resources.getResourceEntryName(R.id.detail_fragment))), 10000)

                wait_until_detail_map_is_finished_loading(uiDevice)
            }
        } catch (e: UiObjectNotFoundException) {
            e.printStackTrace()
        }
    }

    fun navigate_to_update_fragment(uiDevice: UiDevice, mainActivity : FragmentActivity) {
        onView(withId(R.id.detail_fragment)).check(matches(isDisplayed()))
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
            onView(withId(R.id.map_constraint_layout)).perform(scrollTo()/*, click()*/)

            val isDetailMapFinishLoading = uiDevice.wait(Until.hasObject(By.desc(
                DETAIL_MAP_FINISH_LOADING)), 50000)
            assertThat(isDetailMapFinishLoading).isTrue()
        }
    }
}