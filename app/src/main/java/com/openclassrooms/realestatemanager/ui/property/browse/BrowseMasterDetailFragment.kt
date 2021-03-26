package com.openclassrooms.realestatemanager.ui.property.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseMasterDetailBinding
import com.openclassrooms.realestatemanager.ui.navigation.KeepStateNavigator

/**
 * Fragment to handle the display of real estate for tablet.
 */
class BrowseMasterDetailFragment : Fragment() {

    private var _binding: FragmentBrowseMasterDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBrowseMasterDetailBinding.inflate(inflater, container, false)

        val master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment?

        val masterNavigator = KeepStateNavigator(requireContext(), master!!.childFragmentManager, R.id.master_nav_fragment)
        master.navController.navigatorProvider.addNavigator(masterNavigator)

        master?.let {
            val navController = it.navController
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.real_estate_master_navigation)

            it.navController.graph = graph
        }

        val detail = childFragmentManager.findFragmentById(R.id.detail_nav_fragment) as NavHostFragment?

        val detailNavigator = KeepStateNavigator(requireContext(), detail!!.childFragmentManager, R.id.detail_nav_fragment)
        detail.navController.navigatorProvider.addNavigator(detailNavigator)

        detail?.let {
            val navController = it.navController
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.real_estate_detail_navigation)

            it.navController.graph = graph
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}