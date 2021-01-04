package com.openclassrooms.realestatemanager.view.realestate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentRealEstateMasterDetailBinding

/**
 * Fragment to handle the display of real estate for tablet.
 */
class RealEstateMasterDetailFragment : Fragment() {

    private var _binding: FragmentRealEstateMasterDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRealEstateMasterDetailBinding.inflate(inflater, container, false)

        val master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment?
        master?.let {
            val navController = it.navController
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.real_estate_master_navigation)

            it.navController.graph = graph
        }

        val detail = childFragmentManager.findFragmentById(R.id.detail_nav_fragment) as NavHostFragment?
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