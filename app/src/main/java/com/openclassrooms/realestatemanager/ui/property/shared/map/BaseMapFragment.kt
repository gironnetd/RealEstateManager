package com.openclassrooms.realestatemanager.ui.property.shared.map

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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentMapBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseBrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil.bitmapDescriptorFromVector
import com.openclassrooms.realestatemanager.util.Constants
import java.io.File
import java.util.*

abstract class BaseMapFragment : BaseFragment(R.layout.fragment_map), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    lateinit var mMap: GoogleMap
    var isMapInitialized = ::mMap.isInitialized

    lateinit var clusterManager: ClusterManager<CustomClusterItem>

    var selectedItem: CustomClusterItem? = null

    lateinit var items: LinkedHashMap<CustomClusterItem, Boolean>
    var markers : MutableList<Marker> = mutableListOf()

    protected var _binding: FragmentMapBinding? = null
    val binding get() = _binding!!

    lateinit var innerInflater: LayoutInflater

    abstract fun properties(): List<Property>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        applyDisposition()
        return binding.root
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

    fun initializeMap() {
        activity?.runOnUiThread {
            (this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment)
                .getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        val location = CameraUpdateFactory.newLatLngZoom(defaultLocation, INITIAL_ZOOM_LEVEL)
        googleMap.moveCamera(location)
        mMap = googleMap

        clusterManager = ClusterManager(context, mMap)
        mMap.setOnCameraIdleListener(clusterManager)

        clusterManager.renderer = object: CustomClusterRenderer(requireContext(), mMap, clusterManager) {

            override fun getColor(clusterSize: Int): Int {
                return TypedValue().apply {
                    innerInflater.context.theme.resolveAttribute(R.attr.colorPrimary, this, true)
                }.data
            }

            override fun onBeforeClusterItemRendered(item: CustomClusterItem, markerOptions: MarkerOptions) {
                markerOptions.icon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_not_selected))
                super.onBeforeClusterItemRendered(item, markerOptions)
            }

            override fun onClusterItemRendered(clusterItem: CustomClusterItem, marker: Marker) {
                if(!markers.contains(marker)) {
                    markers.add(marker)

                    selectedItem?.let { selectedItem ->
                        if(marker.title == selectedItem.title) {
                            if(!items[selectedItem]!!) {
                                marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_selected))
                                marker.showInfoWindow()
                                items[selectedItem] = true
                                mMap.setContentDescription(INFO_WINDOW_SHOWN)
                            } else {
                                marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_not_selected))
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

        properties().forEach { property ->
            val item = CustomClusterItem(property.address.latitude, property.address.longitude,
                property.address.street, "", property.id)
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
                marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_not_selected))
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

                mMap.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
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

        this.parentFragment?.parentFragment?.let { parentFragment ->
            (parentFragment as BaseBrowseFragment?)?.let { masterDetailFragment ->
                clusterManager.setOnClusterItemInfoWindowClickListener { item ->
                    val bundle: Bundle = if(masterDetailFragment.detail.childFragmentManager
                            .findFragmentByTag(R.id.navigation_detail.toString()) != null) {

                        val detailFragment: PropertyDetailFragment = masterDetailFragment.detail.childFragmentManager
                            .findFragmentByTag(R.id.navigation_detail.toString()) as PropertyDetailFragment

                        detailFragment.showDetails(item.getTag())
                        bundleOf(Constants.FROM to BaseMapFragment::class.java.name)
                    } else {
                        val propertyId = item.getTag()
                        bundleOf(
                            Constants.FROM to BaseMapFragment::class.java.name,
                            Constants.PROPERTY_ID to propertyId
                        )
                    }
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_detail, bundle)
                    (requireActivity() as MainActivity).binding.toolBar.visibility = GONE
                    masterDetailFragment.binding.segmentedcontrol.buttonContainer.visibility = GONE
                }
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapLoaded() {
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker?): View? {
                return null
            }

            override fun getInfoContents(marker: Marker?): View {
                val markerView = layoutInflater.inflate(R.layout.layout_marker, binding.root, false)

                val title = markerView.findViewById<TextView>(R.id.property_address_street)
                title.text = marker!!.title

                val mainPhoto = markerView.findViewById<ImageView>(R.id.main_photo)

                selectedItem?.let { selectedItem ->
                    val property = properties().single { property -> property.id == selectedItem.getTag() }

                    val photo = property.photos.single { photo -> photo.mainPhoto }
                    photo.propertyId = property.id

                    val mainPhotoFile = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                    mainPhoto.setImageURI(mainPhotoFile.toUri())
                }

                return markerView
            }
        })
        binding.mapFragment.contentDescription = MAP_FINISH_LOADING
    }

    fun zoomOnMarkerPosition(propertyId: String) {
        val property = properties().single { property -> property.id == propertyId }
        selectedItem  = items.keys.single { item -> item.getTag() == property.id }

        selectedItem?.let { selectedItem ->
            if(mMap.cameraPosition.zoom == 10f) {
                var cameraUpdate = CameraUpdateFactory.newLatLng(
                    LatLng(selectedItem.position.latitude, selectedItem.position.longitude))

                mMap.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(selectedItem.position.latitude, selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)

                        mMap.animateCamera(cameraUpdate, 2500, object : GoogleMap.CancelableCallback {
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
    }

    fun showOrHideInfoWindow() {
        val isInfoWindowShown = items[selectedItem]!!

        for ((item, _) in items) {
            items[item] = false
            markers.forEach { marker -> marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_not_selected)) }
        }

        var cameraUpdate: CameraUpdate
        if(!isInfoWindowShown) {
            selectedItem?.let { selectedItem ->
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(selectedItem.position.latitude,
                    selectedItem.position.longitude), (DEFAULT_ZOOM + 3))

                if(mMap.cameraPosition.zoom == 10f) {
                    cameraUpdate = CameraUpdateFactory.newLatLng(
                        LatLng(selectedItem.position.latitude, selectedItem.position.longitude))

                    mMap.animateCamera(cameraUpdate, object: GoogleMap.CancelableCallback {
                        override fun onCancel() {}
                        override fun onFinish() {
                            cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                LatLng(selectedItem.position.latitude, selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)

                            mMap.animateCamera(cameraUpdate, 2500, null)
                        }
                    })
                } else {
                    mMap.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
                        override fun onCancel() {}
                        override fun onFinish() {
                            val marker = markers.find { marker -> marker.title == selectedItem.title }
                            marker?.let {
                                if (!items[selectedItem]!!) {
                                    marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_selected))
                                    it.showInfoWindow()
                                    items[selectedItem] = true
                                    mMap.setContentDescription(INFO_WINDOW_SHOWN)
                                }
                            }
                        }
                    })
                }
            }
        } else {
            selectedItem?.let { selectedItem ->
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(selectedItem.position.latitude,
                    selectedItem.position.longitude), DEFAULT_ZOOM + 1.5f)
                mMap.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        val marker = markers.find { marker -> marker.title == selectedItem.title }
                        marker?.let {
                            marker.setIcon(bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_not_selected))
                            marker.hideInfoWindow()
                            items[selectedItem] = false
                            mMap.setContentDescription(NO_INFO_WINDOW_SHOWN)
                        }
                    }
                })
            }
        }
    }

    override fun initializeToolbar() {}

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