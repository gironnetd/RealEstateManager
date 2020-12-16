package com.openclassrooms.realestatemanager.view.realestate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}