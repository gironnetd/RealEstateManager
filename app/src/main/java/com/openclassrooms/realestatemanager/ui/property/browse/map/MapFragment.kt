package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesViewModel
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.GlideManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject

/**
 * Fragment to display real estates on map.
 */
class MapFragment @Inject constructor(
        viewModelFactory: ViewModelProvider.Factory,
        val requestManager: GlideManager,
) : BaseFragment(R.layout.fragment_map, viewModelFactory),
        OnMapReadyCallback, OnMapLoadedCallback,
        BaseView<PropertiesIntent, PropertiesUiModel> {

    private val propertiesViewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

    lateinit var mMap: GoogleMap
    lateinit var clusterManager: ClusterManager<CustomClusterItem>
    lateinit var selectedItem: CustomClusterItem

    lateinit var items: LinkedHashMap<CustomClusterItem, Boolean>
    var markers : MutableList<Marker> = mutableListOf()

    private var _binding: FragmentMapBinding? = null
    val binding get() = _binding!!

    private val loadConversationsIntentPublisher =
        PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        applyDisposition()
        if(properties.isNotEmpty() && !::mMap.isInitialized) {
            initializeMap()
        } else if(properties.isEmpty() || !::mMap.isInitialized) {
            compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
            propertiesViewModel.processIntents(intents())
        }
        return binding.root
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisposition()
    }

    private fun applyDisposition() {

        this.parentFragment?.let {
            val detailFragment = this.parentFragment as NavHostFragment

            if(resources.getBoolean(R.bool.isMasterDetail)) {

                screenWidth = screenWidth(requireActivity())

                binding.mapFragment.apply {
                    val detailWidthWeight = TypedValue()
                    resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
                    layoutParams.width = (screenWidth * detailWidthWeight.float).toInt()
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            if(!resources.getBoolean(R.bool.isMasterDetail)) {
                val detailLayoutParams =
                    FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
                        .apply {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                            height = ViewGroup.LayoutParams.MATCH_PARENT
                        }

                detailFragment.requireView().apply {
                    layoutParams = detailLayoutParams
                }

                binding.mapFragment.apply {
                    layoutParams = detailLayoutParams
                }
            }
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
                                mMap.setContentDescription(INFO_WINDOW_SHOWN)
                            } else {
                                marker.hideInfoWindow()
                                items[selectedItem] = false
                                mMap.setContentDescription(NO_INFO_WINDOW_SHOWN)
                            }
                        }
                    }
                }
                super.onClusterItemRendered(clusterItem, marker)
            }
        }

        mMap.setContentDescription(MAP_NOT_FINISH_LOADING)
        mMap.setOnMapLoadedCallback(this)

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

            mMap.setContentDescription(NO_INFO_WINDOW_SHOWN)

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
            var cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(item.position.latitude, item.position.longitude), (DEFAULT_ZOOM + 1.5f))

            if(mMap.cameraPosition.zoom == 10f) {
                cameraUpdate = CameraUpdateFactory.newLatLng(
                    LatLng(item.position.latitude, item.position.longitude))

                mMap.animateCamera(cameraUpdate, object : CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(item.position.latitude, item.position.longitude), DEFAULT_ZOOM + 1.5f)

                        mMap.animateCamera(cameraUpdate, 2500, null)
                    }
                })
            } else {
                mMap.animateCamera(cameraUpdate)
            }
            true
        }

        this.parentFragment?.parentFragment?.let {
            val masterDetailFragment = this.parentFragment?.parentFragment as BrowseFragment

            clusterManager.setOnClusterItemInfoWindowClickListener { item ->

                if(masterDetailFragment.detail.childFragmentManager
                        .findFragmentByTag(R.id.navigation_detail.toString()) != null) {

                    val detailFragment: DetailFragment = masterDetailFragment.detail.childFragmentManager
                        .findFragmentByTag(R.id.navigation_detail.toString()) as DetailFragment

                    detailFragment.showDetails(item.getTag())
                    val bundle = bundleOf(FROM to MapFragment::class.java.name,)
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_detail, bundle)
                } else {
                    val propertyId = item.getTag()
                    val bundle = bundleOf(FROM to MapFragment::class.java.name,
                        PROPERTY_ID to propertyId
                    )
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_detail, bundle)
                }
                masterDetailFragment.binding.buttonContainer.visibility = GONE
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapLoaded() {
        mMap.setInfoWindowAdapter(object : InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker?): View? {
                return null
            }

            override fun getInfoContents(marker: Marker?): View {
                val markerView = layoutInflater.inflate(R.layout.layout_marker, null)

                val title = markerView.findViewById<TextView>(R.id.property_address_street)
                title.text = marker!!.title

                val mainPhoto = markerView.findViewById<ImageView>(R.id.main_photo)

                val property = properties.single { property -> property.id == selectedItem.getTag() }

                val photo = property.photos.single { photo -> photo.mainPhoto }
                photo.propertyId = property.id

                val mainPhotoFile = File(photo.storageLocalDatabase(requireContext(), true))

                mainPhoto.setImageURI(mainPhotoFile.toUri())
//                val gsReference = Firebase.storage.getReferenceFromUrl(photo.storageUrl(isThumbnail = true))
//
//                Completable.fromAction {
//                    requestManager.setImage(gsReference, mainPhoto, true)
//                }.subscribeOn(SchedulerProvider.io()).blockingAwait()

                return markerView
            }
        })
        mMap.setContentDescription(MAP_FINISH_LOADING)
    }

    fun zoomOnMarkerPosition(propertyId: String) {
        val property = properties.single { property -> property.id == propertyId }
        selectedItem  = items.keys.single { item -> item.getTag() == property.id }

        if(mMap.cameraPosition.zoom == 10f) {
            var cameraUpdate = CameraUpdateFactory.newLatLng(
                    LatLng(selectedItem.position.latitude, selectedItem.position.longitude))

            mMap.animateCamera(cameraUpdate, object : CancelableCallback {
                override fun onCancel() {}
                override fun onFinish() {
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(selectedItem.position.latitude, selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)

                    mMap.animateCamera(cameraUpdate, 2500, object : CancelableCallback {
                        override fun onCancel() {}
                        override fun onFinish() {
                            showOrHideInfoWindow()
                        }
                    })
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
                cameraUpdate = CameraUpdateFactory.newLatLng(
                    LatLng(selectedItem.position.latitude, selectedItem.position.longitude))

                mMap.animateCamera(cameraUpdate, object: CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(selectedItem.position.latitude, selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)

                        mMap.animateCamera(cameraUpdate, 2500, null)
                    }
                })
            } else {
                mMap.animateCamera(cameraUpdate, object : CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        val marker = markers.find { marker -> marker.title == selectedItem.title }
                        marker?.let {
                            if (!items[selectedItem]!!) {
                                it.showInfoWindow()

                                items[selectedItem] = true
                                mMap.setContentDescription(INFO_WINDOW_SHOWN)
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
                        mMap.setContentDescription(NO_INFO_WINDOW_SHOWN)
                    }
                }
            })
        }
    }

    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment

            browseFragment.binding.toolBar.setNavigationOnClickListener {
                val mainActivity = activity as MainActivity
                if (!mainActivity.binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mainActivity.binding.drawerLayout.openDrawer(GravityCompat.START)
                } else {
                    mainActivity.binding.drawerLayout.closeDrawer(GravityCompat.START)
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

        private var paris = LatLng(48.862725, 2.287592)
        var defaultLocation = paris

        // constant variable to perform ui automator testing
        const val MAP_NOT_FINISH_LOADING = "map_not_finish_loading"
        const val MAP_FINISH_LOADING = "map_finish_loading"
        const val INFO_WINDOW_SHOWN = "info_window_shown"
        const val NO_INFO_WINDOW_SHOWN = "no_info_window_shown"
    }
}