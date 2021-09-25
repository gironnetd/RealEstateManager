package com.openclassrooms.realestatemanager.ui

import android.content.SharedPreferences
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.viewmodels.FakePropertiesViewModelFactory
import com.openclassrooms.realestatemanager.util.NavigationHelper
import org.hamcrest.core.AllOf.allOf
import javax.inject.Inject

open class BaseFragmentTests: BaseMainActivityTests() {

    @Inject lateinit var uiDevice: UiDevice
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory

    lateinit var browseFragment: BrowseFragment

    var isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

    var leChesnay = LatLng(48.82958536116524, 2.125609030745346)
    var itemPosition = -1

    open fun navigate_to_detail_fragment() {
        when(isMasterDetail) {
            true -> navigate_to_detail_fragment_in_master_detail_mode()
            false -> navigate_to_detail_fragment_in_normal_mode()
        }
    }

    open fun navigate_to_detail_fragment_in_normal_mode() {
        NavigationHelper.navigate_to_detail_fragment_in_normal_mode(itemPosition)
    }

    open fun navigate_to_detail_fragment_in_master_detail_mode() {
        NavigationHelper.navigate_to_detail_fragment_in_master_detail_mode(
            testApplication, uiDevice, mainActivity, browseFragment, itemPosition, fakeProperties)
    }

    open fun navigate_to_update_fragment() {
        NavigationHelper.navigate_to_update_fragment(uiDevice, mainActivity)
    }

    open fun obtainDetailFragment(): PropertyDetailFragment {
        return browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyDetailFragment
    }

    open fun obtainUpdateFragment(): PropertyUpdateFragment {
        return browseFragment.detail.childFragmentManager.primaryNavigationFragment as PropertyUpdateFragment
    }

    open fun wait_until_map_is_finished_loading() {
        NavigationHelper.wait_until_map_is_finished_loading(uiDevice)
    }

    open fun wait_until_detail_map_is_finished_loading() {
        NavigationHelper.wait_until_detail_map_is_finished_loading(uiDevice)
    }

    fun click_on_navigate_up_button() {
        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description),
                isDisplayed())
        ).perform(click())
    }

    override fun injectTest(application: TestBaseApplication) {}
}