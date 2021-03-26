package com.openclassrooms.realestatemanager.ui.property.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseBinding
import com.openclassrooms.realestatemanager.ui.navigation.KeepStateNavigator

/**
 * Fragment to handle the display of real estate.
 */
class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isTablet()
    }

    private fun isTablet() {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.real_estate_nav_host_fragment)
                as NavHostFragment

        val navigator = KeepStateNavigator(requireContext(), navHostFragment.childFragmentManager, R.id.real_estate_nav_host_fragment)
        navHostFragment.navController.navigatorProvider.addNavigator(navigator)

        navHostFragment.navController.setGraph(R.navigation.real_estate_navigation)

        val isTablet = context?.resources?.getBoolean(R.bool.isTablet) ?: false
        when {
            isTablet -> {
                navHostFragment.findNavController().navigate(R.id.navigation_real_estate_master_detail)
            }
            else -> {
                navHostFragment.findNavController().navigate(R.id.navigation_real_estate_master)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}