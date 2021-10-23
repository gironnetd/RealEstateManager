package com.openclassrooms.realestatemanager.ui.property.shared

import android.content.res.Configuration
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.shared.list.BaseListFragment
import com.openclassrooms.realestatemanager.ui.property.shared.list.ListAdapter
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment
import com.openclassrooms.realestatemanager.util.Constants

abstract class BaseBrowseFragment :
    BaseFragment(R.layout.fragment_browse),
    ListAdapter.OnItemClickListener {

    protected var browseBinding: FragmentBrowseBinding? = null
    val binding get() = browseBinding!!

    val mainActivity by lazy { activity as FragmentActivity }

    lateinit var master: BaseListFragment
    lateinit var detail: NavHostFragment

    private var callBack: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(propertyId: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        callBack = listener
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureView()
    }

    override fun initializeToolbar() {
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_splash, R.id.navigation_map).build()
        binding.toolBar.inflateMenu(R.menu.menu_action_bar)
        binding.toolBar.setupWithNavController(detail.navController, appBarConfiguration)
    }

    override fun onResume() {
        super.onResume()
        initSegmentedControl()
        configureView()
        val adapter = master.binding.propertiesRecyclerView.adapter as ListAdapter
        adapter.setOnItemClickListener(this)
    }

    override fun onItemClick(propertyId: String) {
        (requireActivity() as MainActivity).onBackPressedCallback.isEnabled = false
        if (resources.getBoolean(R.bool.isMasterDetail)) {
            if (detail.childFragmentManager.primaryNavigationFragment is BaseMapFragment) {
                (detail.childFragmentManager.primaryNavigationFragment as BaseMapFragment)
                    .zoomOnMarkerPosition(propertyId = propertyId)
            }

            if (detail.childFragmentManager.primaryNavigationFragment is PropertyDetailFragment) {
                callBack?.onItemClick(propertyId = propertyId)
            }

            if (detail.childFragmentManager.primaryNavigationFragment is PropertyUpdateFragment) {
                callBack?.onItemClick(propertyId = propertyId)

                val bundle = bundleOf(
                    Constants.FROM to BaseListFragment::class.java.name,
                    Constants.PROPERTY_ID to propertyId
                )

                detail.navController.navigate(R.id.navigation_detail, bundle)
            }
        } else {
            (mainActivity as? MainActivity)?.let { mainActivity ->
                mainActivity.binding.toolBar.visibility = GONE
            }
            binding.toolBar.visibility = VISIBLE
            detail.requireView().visibility = VISIBLE
            master.requireView().visibility = GONE
            binding.resultListFragment.visibility = GONE
            binding.resultDetailNavFragment.visibility = VISIBLE
            binding.resultDetailNavFragment.bringToFront()
            binding.segmentedcontrol.buttonContainer.visibility = GONE

            val bundle = bundleOf(
                Constants.FROM to BaseListFragment::class.java.name,
                Constants.PROPERTY_ID to propertyId
            )

            detail.navController.navigate(R.id.navigation_detail, bundle)
        }
    }

    private fun initSegmentedControl() {
        when (resources.getBoolean(R.bool.isMasterDetail)) {
            true -> { binding.segmentedcontrol.buttonContainer.visibility = GONE }
            false -> { binding.segmentedcontrol.buttonContainer.visibility = VISIBLE }
        }

        when (isDetailFragmentSelected()) {
            false -> { binding.segmentedcontrol.listViewButton.isSelected = true }
            true -> { binding.segmentedcontrol.mapViewButton.isSelected = true }
        }

        if (!binding.segmentedcontrol.listViewButton.isSelected && !binding.segmentedcontrol.mapViewButton.isSelected) {
            binding.segmentedcontrol.listViewButton.isSelected = true
        }

        binding.segmentedcontrol.listViewButton.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                if (binding.segmentedcontrol.mapViewButton.isSelected) {
                    binding.segmentedcontrol.mapViewButton.isSelected = false

                    master.requireView().visibility = VISIBLE
                    binding.resultListFragment.visibility = VISIBLE
                    binding.resultListFragment.bringToFront()
                    binding.segmentedcontrol.root.bringToFront()
                    detail.requireView().visibility = GONE
                    binding.resultDetailNavFragment.visibility = GONE
                    setDetailFragmentSelected(false)
                }
            }
        }

        binding.segmentedcontrol.mapViewButton.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true

                if (binding.segmentedcontrol.listViewButton.isSelected) {
                    binding.segmentedcontrol.listViewButton.isSelected = false

                    detail.navController.navigate(R.id.navigation_map)
                    detail.requireView().visibility = VISIBLE
                    binding.resultDetailNavFragment.visibility = VISIBLE
                    binding.resultDetailNavFragment.bringToFront()
                    binding.segmentedcontrol.root.bringToFront()
                    master.requireView().visibility = GONE
                    binding.resultListFragment.visibility = GONE
                    setDetailFragmentSelected(true)
                }
            }
        }
    }

    private fun configureView() {
        val detailLayoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
                .apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

        val masterLayoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
                .apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }

        if (resources.getBoolean(R.bool.isMasterDetail)) {
            binding.segmentedcontrol.buttonContainer.visibility = GONE

            screenWidth = screenWidth(requireActivity())

            masterLayoutParams.apply {
                resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)
                width = (screenWidth * masterWidthWeight.float).toInt()
            }

            master.requireView().apply {
                layoutParams = masterLayoutParams
                requestLayout()
                visibility = VISIBLE
            }

            detailLayoutParams.apply {
                resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
                width = (screenWidth * detailWidthWeight.float).toInt()
                leftMargin = (screenWidth * masterWidthWeight.float).toInt()
                gravity = GravityCompat.END
            }

            detail.requireView().apply {
                layoutParams = detailLayoutParams
                requestLayout()
                visibility = VISIBLE
            }

            if (detail.childFragmentManager.primaryNavigationFragment is SplashFragment) {
                detail.navController.navigate(R.id.navigation_map)
            }

            master.requireView().visibility = VISIBLE
            binding.resultListFragment.visibility = VISIBLE
            detail.requireView().visibility = VISIBLE
            binding.resultDetailNavFragment.visibility = VISIBLE
        } else if (!resources.getBoolean(R.bool.isMasterDetail)) {
            detail.requireView().apply {
                layoutParams = detailLayoutParams
                requestLayout()
            }

            if (detail.childFragmentManager.primaryNavigationFragment is PropertyDetailFragment ||
                detail.childFragmentManager.primaryNavigationFragment is PropertyUpdateFragment
            ) {
                detail.requireView().visibility = VISIBLE
                binding.segmentedcontrol.buttonContainer.visibility = GONE

                master.requireView().visibility = GONE
            } else {
                binding.segmentedcontrol.buttonContainer.visibility = VISIBLE

                if (binding.segmentedcontrol.listViewButton.isSelected) {
                    master.requireView().visibility = VISIBLE
                    binding.resultListFragment.visibility = VISIBLE
                    binding.resultListFragment.bringToFront()
                    detail.requireView().visibility = GONE
                    binding.resultDetailNavFragment.visibility = GONE
                }

                if (binding.segmentedcontrol.mapViewButton.isSelected) {
                    master.requireView().visibility = GONE
                    binding.resultListFragment.visibility = GONE
                    detail.requireView().visibility = VISIBLE
                    binding.resultDetailNavFragment.visibility = VISIBLE
                    binding.resultDetailNavFragment.bringToFront()
                    binding.segmentedcontrol.root.bringToFront()
                    if (detail.childFragmentManager.primaryNavigationFragment !is BaseMapFragment) {
                        detail.navController.navigate(R.id.navigation_map)
                    }
                }
            }
        }
    }

    abstract fun isDetailFragmentSelected(): Boolean
    abstract fun setDetailFragmentSelected(isSelected: Boolean)
}
