package com.openclassrooms.realestatemanager.view.realestate.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentRealEstateMasterBinding

/**
 * Fragment to handle the display of real estate for smartphone.
 */
class RealEstateMasterFragment : Fragment() {

    private var _binding: FragmentRealEstateMasterBinding? = null
    private val binding get() = _binding!!
    private lateinit var master : NavHostFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRealEstateMasterBinding.inflate(inflater, container, false)

        master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment
        master.let {
            val navController = it.navController
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.real_estate_master_navigation)

            it.navController.graph = graph
        }
        initSegmentedControl()
        return binding.root
    }

    private fun initSegmentedControl() {
        binding.listViewButton.isSelected = true

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