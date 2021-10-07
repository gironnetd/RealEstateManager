package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment

/**
 * Fragment to display real estates on map.
 */
class BrowseMapFragment : BaseMapFragment(), OnMapReadyCallback, OnMapLoadedCallback {

    override fun properties(): List<Property> {
        return properties.value!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Primary))
        _binding = FragmentMapBinding.inflate(innerInflater, container, false)
        super.onCreateView(innerInflater, container, savedInstanceState)
        properties.observe(viewLifecycleOwner) { properties ->
            if(properties.isNotEmpty() && !isMapInitialized) {
                initializeMap()
            }
        }
        return binding.root
    }
}