package com.openclassrooms.realestatemanager.view.realestate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.openclassrooms.realestatemanager.R

/**
 * Fragment to handle the display of real estate for tablet.
 */
class RealEstateMasterDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_real_estate_master_detail, container, false)
    }
}