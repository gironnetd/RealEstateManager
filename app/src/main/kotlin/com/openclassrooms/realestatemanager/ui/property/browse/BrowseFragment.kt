package com.openclassrooms.realestatemanager.ui.property.browse

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.commitNow
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.fragments.browsedetail.BrowseDetailNavHostFragment
import com.openclassrooms.realestatemanager.ui.navigation.browsedetail.BrowseDetailFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.browse.list.BrowseListFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseBrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment

/**
 * Fragment to handle the display of real estate for tablet.
 */
class BrowseFragment : BaseBrowseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.commitNow {
            setReorderingAllowed(true)
            replace(R.id.result_list_fragment, BrowseListFragment())
        }
        master = childFragmentManager.findFragmentById(R.id.result_list_fragment) as BrowseListFragment

        childFragmentManager.commitNow {
            setReorderingAllowed(true)
            replace(R.id.result_detail_nav_fragment, BrowseDetailNavHostFragment())
        }
        detail = childFragmentManager.findFragmentById(R.id.result_detail_nav_fragment) as NavHostFragment
        val detailNavigator = BrowseDetailFragmentNavigator(
            requireContext(),
            detail.childFragmentManager, R.id.result_detail_nav_fragment
        )

        detail.apply {
            navController.navigatorProvider.addNavigator(detailNavigator)
            navController.setGraph(R.navigation.properties_detail_navigation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contextThemeWrapper: Context = ContextThemeWrapper(activity, R.style.AppTheme_Primary)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        browseBinding = FragmentBrowseBinding.inflate(localInflater, container, false)
        super.onCreateView(localInflater, container, savedInstanceState)
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (mainActivity as? MainActivity)?.let { mainActivity ->
                with(mainActivity.binding) {
                    toolBar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                    statusbar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null))
                }
                if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                    if (detail.requireView().visibility == VISIBLE) {
                        if (detail.childFragmentManager.primaryNavigationFragment is BaseMapFragment) {
                            mainActivity.binding.toolBar.visibility = VISIBLE
                        } else {
                            mainActivity.binding.toolBar.visibility = GONE
                        }
                    } else {
                        mainActivity.binding.toolBar.visibility = VISIBLE
                    }
                }

                if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                    if (detail.childFragmentManager.primaryNavigationFragment is BaseMapFragment) {
                        mainActivity.binding.toolBar.visibility = VISIBLE
                    } else {
                        mainActivity.binding.toolBar.visibility = GONE
                        binding.toolBar.visibility = VISIBLE
                    }
                }

                detail.childFragmentManager.primaryNavigationFragment?.let { primaryNavigationFragment ->
                    when (primaryNavigationFragment::class.java) {
                        PropertyDetailFragment::class.java -> {
                            (primaryNavigationFragment as PropertyDetailFragment)
                                .onBackPressedCallback.isEnabled = true
                        }
                        PropertyUpdateFragment::class.java -> {
                            (primaryNavigationFragment as PropertyUpdateFragment)
                                .onBackPressedCallback.isEnabled = true
                        }
                        else -> { mainActivity.onBackPressedCallback.isEnabled = true }
                    }
                }
            }
        } else {
            detail.childFragmentManager.primaryNavigationFragment?.let { primaryNavigationFragment ->
                when (primaryNavigationFragment::class.java) {
                    PropertyDetailFragment::class.java -> {
                        (primaryNavigationFragment as PropertyDetailFragment)
                            .onBackPressedCallback.isEnabled = false
                        if ((requireActivity() as MainActivity).onBackPressedCallback.isEnabled) {
                            (requireActivity() as MainActivity).onBackPressedCallback.isEnabled = false
                        }
                    }
                    PropertyUpdateFragment::class.java -> {
                        (primaryNavigationFragment as PropertyUpdateFragment)
                            .onBackPressedCallback.isEnabled = false
                        if ((requireActivity() as MainActivity).onBackPressedCallback.isEnabled) {
                            (requireActivity() as MainActivity).onBackPressedCallback.isEnabled = false
                        }
                    } else -> { (requireActivity() as MainActivity).onBackPressedCallback.isEnabled = false }
                }
            }
        }
    }

    override fun isDetailFragmentSelected(): Boolean {
        return WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED
    }

    override fun setDetailFragmentSelected(isSelected: Boolean) {
        WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = isSelected
    }

    companion object {
        var WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED: Boolean = false
    }
}
