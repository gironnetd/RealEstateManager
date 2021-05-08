package com.openclassrooms.realestatemanager.ui.property.browse

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.View.*
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseBinding
import com.openclassrooms.realestatemanager.fragments.property.browse.detail.BrowseDetailNavHostFragment
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.navigation.browse.detail.BrowseDetailFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.edit.EditFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListAdapter
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID

/**
 * Fragment to handle the display of real estate for tablet.
 */
class BrowseFragment : BaseFragment(R.layout.fragment_browse, null),
        ListAdapter.OnItemClickListener {

    private var _binding: FragmentBrowseBinding? = null
    val binding get() = _binding!!

    lateinit var master: ListFragment
    lateinit var detail: NavHostFragment

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentBrowseBinding.inflate(inflater, container, false)

        master = childFragmentManager.findFragmentById(R.id.list_fragment) as ListFragment

        detail = childFragmentManager.findFragmentById(R.id.detail_nav_fragment) as NavHostFragment

        val detailNavigator = BrowseDetailFragmentNavigator(requireContext(), detail.childFragmentManager, R.id.detail_nav_fragment)
        detail.navController.navigatorProvider.addNavigator(detailNavigator)

        detail.navController.setGraph(R.navigation.real_estate_detail_navigation)

        initSegmentedControl()
        configureView()

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureView()
    }

    override fun initializeToolbar() {
        val mainActivity = activity as MainActivity
        mainActivity.binding.toolBar.visibility = GONE
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_map)
                .setOpenableLayout(mainActivity.binding.drawerLayout)
                .build()

        mainActivity.setSupportActionBar(binding.toolBar)
        binding.toolBar.setupWithNavController(detail.navController, appBarConfiguration)
    }

    private fun initSegmentedControl() {

        when(resources.getBoolean(R.bool.isMasterDetail)) {
            true -> { binding.buttonContainer.visibility = GONE }
            false -> { binding.buttonContainer.visibility = VISIBLE }
        }

        when(NAV_HOST_FRAGMENT_SELECTED_WHEN_NON_TABLET_MODE) {
            ListFragment::class.java.name -> {
                binding.listViewButton.isSelected = true
            }
            BrowseDetailNavHostFragment::class.java.name -> {
                binding.mapViewButton.isSelected = true
            }
        }

        if(!binding.listViewButton.isSelected && !binding.mapViewButton.isSelected) {
            binding.listViewButton.isSelected = true
        }

        binding.listViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true
                if(binding.mapViewButton.isSelected) {
                    binding.mapViewButton.isSelected = false

                    master.requireView().visibility = VISIBLE
                    detail.requireView().visibility = INVISIBLE
                    NAV_HOST_FRAGMENT_SELECTED_WHEN_NON_TABLET_MODE = ListFragment::class.java.name
                }
            }
        }

        binding.mapViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true

                if(binding.listViewButton.isSelected) {
                    binding.listViewButton.isSelected = false

                    detail.requireView().visibility = VISIBLE
                    master.requireView().visibility = INVISIBLE
                    NAV_HOST_FRAGMENT_SELECTED_WHEN_NON_TABLET_MODE = BrowseDetailNavHostFragment::class.java.name
                }
            }
        }
    }

    private fun configureView() {

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val detailLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        detailLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val masterLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        masterLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        if (resources.getBoolean(R.bool.isMasterDetail)) {

            binding.buttonContainer.visibility = GONE

            val masterWidthWeight = TypedValue()
            resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)
            detailLayoutParams.leftMargin = (screenWidth * masterWidthWeight.float).toInt()

            masterLayoutParams.width = (screenWidth * masterWidthWeight.float).toInt()

            master.requireView().layoutParams = masterLayoutParams
            master.requireView().requestLayout()

            val detailWidthWeight = TypedValue()
            resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
            detailLayoutParams.width = (screenWidth * detailWidthWeight.float).toInt()

            detail.requireView().layoutParams = detailLayoutParams
            detail.requireView().requestLayout()

            detail.requireView().visibility = VISIBLE
            master.requireView().visibility = VISIBLE

        } else if (!resources.getBoolean(R.bool.isMasterDetail)) {

            detail.requireView().layoutParams = detailLayoutParams
            detail.requireView().requestLayout()

            if(detail.childFragmentManager.primaryNavigationFragment is DetailFragment ||
                    detail.childFragmentManager.primaryNavigationFragment is EditFragment ) {
                binding.buttonContainer.visibility = GONE

                master.requireView().visibility = INVISIBLE
                detail.requireView().visibility = VISIBLE
            } else {
                binding.buttonContainer.visibility = VISIBLE

                if(binding.listViewButton.isSelected) {
                    master.requireView().visibility = VISIBLE
                    detail.requireView().visibility = INVISIBLE
                }

                if(binding.mapViewButton.isSelected) {
                    master.requireView().visibility = INVISIBLE
                    detail.requireView().visibility = VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = master.binding.recyclerView.adapter as ListAdapter
        adapter.setOnItemClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_action_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    interface OnItemClickListener {
        fun onItemClick(propertyId: String)
    }

    var callBack: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        callBack = listener
    }

    override fun onItemClick(propertyId: String) {
        if(resources.getBoolean(R.bool.isMasterDetail)) {
            if(detail.childFragmentManager.primaryNavigationFragment is MapFragment) {
                (detail.childFragmentManager.primaryNavigationFragment as MapFragment)
                        .zoomOnMarkerPosition(propertyId = propertyId)
            }

            if(detail.childFragmentManager.primaryNavigationFragment is DetailFragment){
                callBack?.onItemClick(propertyId = propertyId)
            }
        } else {
            val bundle = bundleOf(FROM to ListFragment::class.java.name,
                    PROPERTY_ID to propertyId)
            detail.findNavController().navigate(R.id.navigation_detail, bundle)

            master.requireView().visibility = GONE
            detail.requireView().visibility = VISIBLE
            binding.buttonContainer.visibility = GONE
        }
    }

    companion object {
        var NAV_HOST_FRAGMENT_SELECTED_WHEN_NON_TABLET_MODE = ListFragment::class.java.name
    }
}