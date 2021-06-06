package com.openclassrooms.realestatemanager.ui

import android.graphics.Bitmap
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.maps.model.LatLng
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.update.UpdateFragment
import com.openclassrooms.realestatemanager.util.GlideManager
import com.openclassrooms.realestatemanager.util.NavigationHelper
import com.openclassrooms.realestatemanager.viewmodels.FakePropertiesViewModelFactory
import org.hamcrest.core.AllOf.allOf
import javax.inject.Inject

open class BaseFragmentTests: BaseMainActivityTests() {

    @Inject lateinit var uiDevice: UiDevice
    @Inject lateinit var requestManager: GlideManager
    @Inject lateinit var propertiesViewModelFactory: FakePropertiesViewModelFactory

    lateinit var browseFragment: BrowseFragment

    val isMasterDetail = testApplication.resources.getBoolean(R.bool.isMasterDetail)

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
            uiDevice, mainActivity, browseFragment, itemPosition, fakeProperties)
    }

    open fun navigate_to_update_fragment() {
        NavigationHelper.navigate_to_update_fragment(uiDevice, mainActivity)
    }

    open fun obtainDetailFragment(): DetailFragment {
        return browseFragment.detail.childFragmentManager.primaryNavigationFragment as DetailFragment
    }

    open fun obtainUpdateFragment(): UpdateFragment {
        return browseFragment.detail.childFragmentManager.primaryNavigationFragment as UpdateFragment
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

    fun sameAs(A: Bitmap, B: Bitmap): Boolean {
        // Different types of image
        if (A.config != B.config) return false

        // Different sizes
        if (A.width != B.width) return false
        if (A.height != B.height) return false

        // Allocate arrays - OK because at worst we have 3 bytes + Alpha (?)
        val w = A.width
        val h = A.height
        val argbA = IntArray(w * h)
        val argbB = IntArray(w * h)
        A.getPixels(argbA, 0, w, 0, 0, w, h)
        B.getPixels(argbB, 0, w, 0, 0, w, h)

        // Alpha channel special check
        if (A.config == Bitmap.Config.ALPHA_8) {
            // in this case we have to manually compare the alpha channel as the rest is garbage.
            val length = w * h
            for (i in 0 until length) {
                if (argbA[i] and -0x1000000 != argbB[i] and -0x1000000) {
                    return false
                }
            }
            return true
        }
        return argbA.contentEquals(argbB)
    }


    override fun injectTest(application: TestBaseApplication) {}
}