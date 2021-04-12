package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
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
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback,
        BaseView<PropertiesIntent, PropertiesUiModel> {

    lateinit var mMap: GoogleMap
    lateinit var clusterManager: ClusterManager<CustomClusterItem>
    lateinit var selectedItem: CustomClusterItem

    lateinit var items: LinkedHashMap<CustomClusterItem, Boolean>
    var markers : MutableList<Marker> = mutableListOf()

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
        when (state) {
            is PropertiesUiModel.Success -> {
                if (properties.isEmpty() && state.properties!!.isNotEmpty()) {
                    properties.addAll(state.properties)
                }

                if (properties != state.properties) {
                    properties.clear()
                    properties.addAll(state.properties!!)
                }
                initializeMap()
            }
            else -> { }
        }
    }

    private fun initializeMap() {
        activity?.runOnUiThread {
            (this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment)
                    .getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (::mMap.isInitialized) {
            return
        }

        val location = CameraUpdateFactory.newLatLngZoom(defaultLocation, INITIAL_ZOOM_LEVEL)
        googleMap.moveCamera(location)
        mMap = googleMap

        clusterManager = ClusterManager(context, mMap)
        mMap.setOnCameraIdleListener(clusterManager)

        clusterManager.renderer = object: CustomClusterRenderer(requireContext(), mMap, clusterManager) {

            override fun onClusterItemRendered(clusterItem: CustomClusterItem, marker: Marker) {
                if(!markers.contains(marker)) {
                    markers.add(marker)

                    if(::selectedItem.isInitialized) {
                        if(marker.title == selectedItem.title) {
                            if(!items[selectedItem]!!) {
                                marker.showInfoWindow()
                                items[selectedItem] = true
                                mMap.setContentDescription(INFO_WINDOW_SHOW)
                            } else {
                                marker.hideInfoWindow()
                                items[selectedItem] = false
                                mMap.setContentDescription(NO_INFO_WINDOW_SHOW)
                            }
                        }
                    }
                }
                super.onClusterItemRendered(clusterItem, marker)
            }
        }

        mMap.setContentDescription(GOOGLE_MAP_NOT_FINISH_LOADING)
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        items = linkedMapOf()

        properties.forEach { property ->
            val item = CustomClusterItem(property.address!!.latitude, property.address!!.longitude,
                    property.address!!.street, "", property.id)
            items[item] = false
            clusterManager.addItem(item)
        }
        clusterManager.cluster()

        mMap.setOnMapClickListener {
            if(mMap.cameraPosition.zoom != 10f) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        it, DEFAULT_ZOOM + 1.5f)
                mMap.animateCamera(cameraUpdate)
            }

            for (marker in clusterManager.markerCollection.markers) {
                marker.hideInfoWindow()
            }

            mMap.setContentDescription(NO_INFO_WINDOW_SHOW)

            for ((item, _) in items) {
                items[item] = false
            }
        }

        clusterManager.setOnClusterItemClickListener { selectedItem ->
            this.selectedItem = selectedItem
            showOrHideInfoWindow()
            true
        }

        clusterManager.setOnClusterClickListener { item  ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatLng(item.position.latitude, item.position.longitude), (DEFAULT_ZOOM + 1.5f))

            if(mMap.cameraPosition.zoom == 10f) {
                mMap.animateCamera(cameraUpdate, 2500, null)
            } else {
                mMap.animateCamera(cameraUpdate)
            }
            true
        }

        when(this.parentFragment?.parentFragment?.javaClass?.name) {
            BrowseMasterFragment::class.java.name -> {
                val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment

                clusterManager.setOnClusterItemInfoWindowClickListener { item ->
                    val propertyId = item.getTag()
                    val action = PropertyMapFragmentDirections.navigationDetailAction(
                            from = PropertyMapFragment::class.java.name,
                            propertyId = propertyId
                    )
                    masterFragment.master.findNavController().navigate(action)
                }
            }
            BrowseMasterDetailFragment::class.java.name -> {
                val masterDetailFragment = this.parentFragment?.parentFragment as BrowseMasterDetailFragment

                clusterManager.setOnClusterItemInfoWindowClickListener { item ->
                    val propertyId = item.getTag()
                    val bundle = bundleOf(FROM to PropertyMapFragment::class.java.name,
                            PROPERTY_ID to propertyId
                    )
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_detail, bundle)
                }
            }
        }
        mMap.setContentDescription(GOOGLE_MAP_FINISH_LOADING)
    }

    fun zoomOnMarkerPosition(propertyId: String) {
        val property = properties.single { property -> property.id == propertyId }
        selectedItem  = items.keys.single { item -> item.getTag() == property.id }

        if(mMap.cameraPosition.zoom == 10f) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    LatLng(selectedItem.position.latitude, selectedItem.position.longitude), (DEFAULT_ZOOM + 1.5f))

            mMap.animateCamera(cameraUpdate, 2500, object : CancelableCallback {
                override fun onCancel() {}
                override fun onFinish() {
                    showOrHideInfoWindow()
                }
            })
        } else {
            showOrHideInfoWindow()
        }
    }

    private fun showOrHideInfoWindow() {
        val isInfoWindowShown = items[selectedItem]!!

        for ((item, _) in items) {
            items[item] = false
        }

        var cameraUpdate: CameraUpdate
        if(!isInfoWindowShown) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(selectedItem.position.latitude,
                    selectedItem.position.longitude), (DEFAULT_ZOOM + 3))

            if(mMap.cameraPosition.zoom == 10f) {
                mMap.animateCamera(cameraUpdate, 2500, null)
            } else {
                mMap.animateCamera(cameraUpdate, object : CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        val marker = markers.find { marker -> marker.title == selectedItem.title }
                        marker?.let {
                            if(!items[selectedItem]!!) {
                                it.showInfoWindow()

                                items[selectedItem] = true
                                mMap.setContentDescription(INFO_WINDOW_SHOW)
                            }
                        }
                    }
                })
            }
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(selectedItem.position.latitude,
                    selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)
            mMap.animateCamera(cameraUpdate, object : CancelableCallback {
                override fun onCancel() {}
                override fun onFinish() {
                    val marker = markers.find { marker -> marker.title == selectedItem.title }
                    marker?.let {
                        marker.hideInfoWindow()
                        items[selectedItem] = false
                        mMap.setContentDescription(NO_INFO_WINDOW_SHOW)
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        when(this.parentFragment?.parentFragment?.javaClass?.name) {
            BrowseMasterFragment::class.java.name -> {
                val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment

                masterFragment.binding.buttonContainer.visibility = VISIBLE
                masterFragment.binding.mapViewButton.isSelected = true
                masterFragment.binding.listViewButton.isSelected = false
            }
        }
    }

    override fun initializeToolbar() {
        when(this.parentFragment?.parentFragment?.javaClass?.name) {
            BrowseMasterFragment::class.java.name -> {
                val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment

                masterFragment.binding.toolBar.setNavigationOnClickListener {
                    val mainActivity = activity as MainActivity
                    if (!mainActivity.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mainActivity.binding.drawerLayout.openDrawer(GravityCompat.START)
                    } else {
                        mainActivity.binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
            }
            BrowseMasterDetailFragment::class.java.name -> {
                val masterDetailFragment = this.parentFragment?.parentFragment as BrowseMasterDetailFragment

                masterDetailFragment.binding.toolBar.setNavigationOnClickListener {
                    val mainActivity = activity as MainActivity
                    if (!mainActivity.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mainActivity.binding.drawerLayout.openDrawer(GravityCompat.START)
                    } else {
                        mainActivity.binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        var DEFAULT_ZOOM: Float = 15f
        var INITIAL_ZOOM_LEVEL = 10f

        var paris = LatLng(48.862725, 2.287592)
        var defaultLocation = paris

        // constant variable to perform ui automator testing
        const val GOOGLE_MAP_NOT_FINISH_LOADING = "google_maps_not_finish_loading"
        const val GOOGLE_MAP_FINISH_LOADING = "google_maps_finish_loading"
        const val INFO_WINDOW_SHOW = "info_window_shown"
        const val NO_INFO_WINDOW_SHOW = "no_info_window_shown"
    }
}