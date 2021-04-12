package com.openclassrooms.realestatemanager.ui.simulation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentSimulationBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment

/**
 * Fragment make simulation
 */
class SimulationFragment : BaseFragment(R.layout.fragment_simulation, null) {

    private var _binding: FragmentSimulationBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        _binding = FragmentSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initializeToolbar() {
        val mainActivity = activity as MainActivity
        mainActivity.binding.toolBar.visibility = View.VISIBLE
        mainActivity.setSupportActionBar(mainActivity.binding.toolBar)

        mainActivity.binding.toolBar.setupWithNavController(
                mainActivity.navController,
                mainActivity.appBarConfiguration)
    }
}