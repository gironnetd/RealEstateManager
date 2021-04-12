package com.openclassrooms.realestatemanager.ui.property.browse

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseMasterBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.navigation.browse.master.BrowseMasterFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.BaseFragment

/**
 * Fragment to handle the display of real estate for smartphone.
 */
class BrowseMasterFragment : BaseFragment(R.layout.fragment_browse_master, null) {

    private var _binding: FragmentBrowseMasterBinding? = null
    val binding get() = _binding!!
    lateinit var master: NavHostFragment

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentBrowseMasterBinding.inflate(inflater, container, false)

        master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment

        val navigator = BrowseMasterFragmentNavigator(requireContext(), master.childFragmentManager, R.id.master_nav_fragment)
        master.navController.navigatorProvider.addNavigator(navigator)

        master.navController.setGraph(R.navigation.real_estate_master_navigation)

        initSegmentedControl()
        return binding.root
    }

    override fun initializeToolbar() {
        val mainActivity = activity as MainActivity
        mainActivity.binding.toolBar.visibility = View.GONE
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_list, R.id.navigation_map)
                .setOpenableLayout(mainActivity.binding.drawerLayout)
                .build()

        mainActivity.setSupportActionBar(binding.toolBar)
        binding.toolBar.setupWithNavController(master.navController, appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(master.navController) ||
                super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_action_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSegmentedControl() {
        binding.buttonContainer.bringToFront()
        binding.toolBar.bringToFront()

        when(master.navController.currentDestination?.id) {
            R.id.navigation_list -> binding.listViewButton.isSelected = true
            R.id.navigation_map -> binding.mapViewButton.isSelected = true
            else -> { }
        }

        binding.listViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true
                if(binding.mapViewButton.isSelected) {
                    binding.mapViewButton.isSelected = false
                    master.navController.navigate(R.id.navigation_list)
                }
            }
        }

        binding.mapViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true

                if(binding.listViewButton.isSelected) {
                    binding.listViewButton.isSelected = false
                    master.navController.navigate(R.id.navigation_map)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}