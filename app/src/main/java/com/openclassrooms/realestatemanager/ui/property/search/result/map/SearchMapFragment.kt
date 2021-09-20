package com.openclassrooms.realestatemanager.ui.property.search.result.map

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment.Companion.searchedProperties
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment

/**
 * Fragment to display real estates on map.
 */
class SearchMapFragment : BaseMapFragment(), OnMapReadyCallback, OnMapLoadedCallback {

    override fun properties(): List<Property> {
        return searchedProperties.value!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Tertiary))
        _binding = FragmentMapBinding.inflate(innerInflater, container, false)
        super.onCreateView(innerInflater, container, savedInstanceState)
        searchedProperties.observe(viewLifecycleOwner) { properties ->

            if(properties.isNotEmpty() && binding.mapFragment.contentDescription != MAP_FINISH_LOADING) {
                initializeMap()
            }

            if(properties.isNotEmpty() && binding.mapFragment.contentDescription == MAP_FINISH_LOADING) {
                super.onMapReady(mMap)
            }

            if(properties.isEmpty()) {
                mMap.clear()
                selectedItem = null
                items.clear()
                markers.clear()
                val location = CameraUpdateFactory.newLatLngZoom(defaultLocation, INITIAL_ZOOM_LEVEL)
                mMap.moveCamera(location)
            }
        }
        return binding.root
    }

}