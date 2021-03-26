package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Fragment to display real estates on map.
 */
class PropertyMapFragment
@Inject
constructor(
        viewModelFactory: ViewModelProvider.Factory,
) : BasePropertyFragment(R.layout.fragment_map, viewModelFactory),
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, BaseView<PropertiesIntent, PropertiesUiModel> {

    lateinit var mMap: GoogleMap

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    companion object {
        private const val DEFAULT_ZOOM: Float = 17f
        const val GOOGLE_MAP_NOT_FINISH_LOADING = "google_maps_not_finish_loading"
        const val GOOGLE_MAP_FINISH_LOADING = "google_maps_finish_loading"
    }

    override fun onMapReady(p0: GoogleMap?) {
        TODO("Not yet implemented")
    }

    override fun onMapLoaded() {
        TODO("Not yet implemented")
    }

    override fun intents(): Observable<PropertiesIntent> {
        TODO("Not yet implemented")
    }

    override fun render(state: PropertiesUiModel) {
        TODO("Not yet implemented")
    }
}