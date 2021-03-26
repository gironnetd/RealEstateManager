package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import com.openclassrooms.realestatemanager.util.EspressoIdlingResource
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
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

    private val defaultLocation = LatLng(48.82958536116524, 2.125609030745346)
    lateinit var mMap: GoogleMap
    lateinit var markers: LinkedHashMap<String, Marker>

    private var _binding: FragmentMapBinding? = null

    private val loadConversationsIntentPublisher =
            PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapBinding.bind(view)
        if(properties.isNotEmpty() && !::mMap.isInitialized) {
            initializeMap()
        } else if(properties.isEmpty() || !::mMap.isInitialized) {
            compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
            propertiesViewModel.processIntents(intents())
        }
    }

    override fun intents(): Observable<PropertiesIntent> {
        return Observable.merge(initialIntent(), loadPropertiesIntentPublisher()
        )
    }

    private fun initialIntent(): Observable<PropertiesIntent.InitialIntent> {
        return Observable.just(PropertiesIntent.InitialIntent)
    }

    private fun loadPropertiesIntentPublisher(): Observable<PropertiesIntent.LoadPropertiesIntent> {
        return loadConversationsIntentPublisher
    }

    override fun render(state: PropertiesUiModel) {
        when  {
            state.inProgress -> {
            }
            state is PropertiesUiModel.Success -> {
                if(properties.isEmpty() && state.properties!!.isNotEmpty()) {
                    properties.addAll(state.properties)
                }

                if(properties != state.properties) {
                    properties.clear()
                    properties.addAll(state.properties!!)
                }
                initializeMap()
            }
            state is PropertiesUiModel.Failed -> { }
            state is PropertiesUiModel.Idle -> { }
            else -> { }
        }
    }

    private fun initializeMap() {
        activity?.runOnUiThread {
            EspressoIdlingResource.increment()
            (this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment)
                    .getMapAsync(this)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLoadedCallback(this)
        mMap.setContentDescription(GOOGLE_MAP_NOT_FINISH_LOADING)

        val location = CameraUpdateFactory.newLatLngZoom(
                defaultLocation, DEFAULT_ZOOM)
        mMap.animateCamera(location)

        markers = linkedMapOf()

        properties.forEach { property ->
            val marker = mMap.addMarker(MarkerOptions()
                    .position(LatLng(property.address!!.latitude, property.address!!.longitude))
                    .title(property.address!!.street))
            markers[property.address!!.street] = marker
        }

        mMap.setOnMapClickListener {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    it, DEFAULT_ZOOM - 0.5f)
            mMap.animateCamera(cameraUpdate)

            markers.values.forEach { marker ->
                marker.tag = null
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val cameraUpdate: CameraUpdate
            if(marker.tag == null) {
                marker.tag = 1
                marker.showInfoWindow()
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(marker.position.latitude, marker.position.longitude), (DEFAULT_ZOOM + 1))
                mMap.animateCamera(cameraUpdate)
            } else {
                marker.tag = null
                marker.hideInfoWindow()
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(marker.position.latitude, marker.position.longitude),
                        DEFAULT_ZOOM - 0.5f)
                mMap.animateCamera(cameraUpdate)
            }
            true
        }
    }

    override fun onMapLoaded() {
        mMap.setContentDescription(GOOGLE_MAP_FINISH_LOADING)
        EspressoIdlingResource.decrement()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        private const val DEFAULT_ZOOM: Float = 17f
        const val GOOGLE_MAP_NOT_FINISH_LOADING = "google_maps_not_finish_loading"
        const val GOOGLE_MAP_FINISH_LOADING = "google_maps_finish_loading"
    }
}