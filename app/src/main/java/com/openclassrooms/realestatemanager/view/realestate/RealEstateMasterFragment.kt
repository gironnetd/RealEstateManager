package com.openclassrooms.realestatemanager.view.realestate

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRealEstateMasterBinding.inflate(inflater, container, false)

        val master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment?
        if(master != null){
            val navController = master.navController
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.real_estate_master_navigation)

            master.navController.graph = graph
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}