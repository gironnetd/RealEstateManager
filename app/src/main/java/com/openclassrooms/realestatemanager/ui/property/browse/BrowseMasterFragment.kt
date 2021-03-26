package com.openclassrooms.realestatemanager.ui.property.browse

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseMasterBinding
import com.openclassrooms.realestatemanager.ui.navigation.KeepStateNavigator
import com.openclassrooms.realestatemanager.ui.property.browse.list.PropertyListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.PropertyMapFragment

/**
 * Fragment to handle the display of real estate for smartphone.
 */
class BrowseMasterFragment : Fragment() {

    private var _binding: FragmentBrowseMasterBinding? = null
    private val binding get() = _binding!!
    private lateinit var master: NavHostFragment

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentBrowseMasterBinding.inflate(inflater, container, false)

        master = childFragmentManager.findFragmentById(R.id.master_nav_fragment) as NavHostFragment

        val navigator = KeepStateNavigator(requireContext(), master.childFragmentManager, R.id.master_nav_fragment)
        master.navController.navigatorProvider.addNavigator(navigator)

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
        binding.buttonContainer.bringToFront()

        val currentFragment = master.childFragmentManager.fragments[0]

        when(currentFragment.javaClass.name) {
            PropertyListFragment::class.java.name -> {
                binding.listViewButton.isSelected = true
                initializeListLayout()
            }
            PropertyMapFragment::class.java.name -> {
                binding.mapViewButton.isSelected = true
                initializeMapLayout()
            }
            else -> {}
        }

        binding.listViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true
                if(binding.mapViewButton.isSelected) {
                    binding.mapViewButton.isSelected = false

                    binding.masterNavFragment.animate()
                            .alpha(0.0f)
                            .setDuration(200)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {

                                    master.navController.navigate(R.id.navigation_list)

                                    initializeListLayout()

                                    binding.masterNavFragment.animate()
                                            .alpha(1.0f)
                                            .setDuration(200)
                                            .setListener(null)
                                    super.onAnimationEnd(animation)
                                }
                            })
                }
            }
        }

        binding.mapViewButton.setOnClickListener {
            if(!it.isSelected) {
                it.isSelected = true

                if(binding.listViewButton.isSelected) {
                    binding.listViewButton.isSelected = false

                    binding.masterNavFragment.animate()
                            .alpha(0.0f)
                            .setDuration(200)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {

                                    master.navController.navigate(R.id.navigation_map)

                                    initializeMapLayout()

                                    binding.masterNavFragment.animate()
                                            .alpha(1.0f)
                                            .setDuration(200)
                                            .setListener(null)
                                    super.onAnimationEnd(animation)

                                }
                            })
                }
            }
        }
    }

    private fun initializeListLayout() {
        var params = binding.masterNavFragment.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = binding.buttonContainer.id
        params.topMargin = binding.buttonContainer.bottom + 20
        binding.masterNavFragment.requestLayout()
    }

    private fun initializeMapLayout() {
        var params = binding.masterNavFragment.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = binding.realEstateMasterFragment.id
        params.topMargin = 0
        binding.masterNavFragment.requestLayout()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}