package com.openclassrooms.realestatemanager.ui.property.browse

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseMasterDetailBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.navigation.browse.detail.BrowseMasterDetailFragmentNavigator
import com.openclassrooms.realestatemanager.ui.navigation.browse.master.BrowseMasterFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.PropertyListAdapter
import com.openclassrooms.realestatemanager.ui.property.browse.list.PropertyListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.PropertyMapFragment

/**
 * Fragment to handle the display of real estate for tablet.
 */
class BrowseMasterDetailFragment : BasePropertyFragment(R.layout.fragment_browse_master_detail, null),
        PropertyListAdapter.OnItemClickListener {

    private var _binding: FragmentBrowseMasterDetailBinding? = null
    val binding get() = _binding!!

    lateinit var master: NavHostFragment
    lateinit var detail: NavHostFragment

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBrowseMasterDetailBinding.inflate(inflater, container, false)

        master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment

        val masterNavigator = BrowseMasterFragmentNavigator(requireContext(), master.childFragmentManager, R.id.master_nav_fragment)
        master.navController.navigatorProvider.addNavigator(masterNavigator)

        master.navController.setGraph(R.navigation.real_estate_master_navigation)

        detail = childFragmentManager.findFragmentById(R.id.detail_nav_fragment) as NavHostFragment

        val detailNavigator = BrowseMasterDetailFragmentNavigator(requireContext(), detail.childFragmentManager, R.id.detail_nav_fragment)
        detail.navController.navigatorProvider.addNavigator(detailNavigator)

        detail.navController.setGraph(R.navigation.real_estate_detail_navigation)

        return binding.root
    }

    override fun initializeToolbar() {
        val mainActivity = activity as MainActivity
        mainActivity.binding.toolBar.visibility = View.GONE
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_map)
                .setOpenableLayout(mainActivity.binding.drawerLayout)
                .build()

        mainActivity.setSupportActionBar(binding.toolBar)
        binding.toolBar.setupWithNavController(detail.navController, appBarConfiguration)
    }

    override fun onResume() {
        super.onResume()
        val adapter = (master.childFragmentManager.primaryNavigationFragment as PropertyListFragment)
                .binding.recyclerView.adapter as PropertyListAdapter
        adapter.setOnItemClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_action_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onItemClick(propertyId: String) {
        (detail.childFragmentManager.primaryNavigationFragment as PropertyMapFragment)
                .zoomOnMarkerPosition(propertyId = propertyId)
    }
}