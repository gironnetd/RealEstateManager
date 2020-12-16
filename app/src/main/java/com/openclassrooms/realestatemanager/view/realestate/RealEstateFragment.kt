package com.openclassrooms.realestatemanager.view.realestate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentRealEstateBinding

/**
 * Fragment to handle the display of real estate.
 */
class RealEstateFragment : Fragment() {

    private var _binding: FragmentRealEstateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRealEstateBinding.inflate(inflater, container, false)
        val isTablet = context?.resources?.getBoolean(R.bool.isTablet) ?: false
        when {
            isTablet -> {
                findNavController().navigate(R.id.navigation_master_detail_real_estate)
            }
            else -> {
                findNavController().navigate(R.id.navigation_list)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}