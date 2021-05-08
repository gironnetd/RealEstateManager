package com.openclassrooms.realestatemanager.ui.property.browse.detail

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

/**
 * Fragment to display and edit a real estate.
 */
class DetailFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    val requestManager: GlideManager,
) : BaseFragment(R.layout.fragment_detail, viewModelFactory),
    OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, BrowseFragment.OnItemClickListener {

    private var _binding: FragmentDetailBinding? = null
    val binding get() = _binding!!

    lateinit var property: Property

    private lateinit var editItem: MenuItem

    lateinit var mMap: GoogleMap

    lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        showDetails(arguments?.getString(PROPERTY_ID))

        onBackPressedCallback()

        activity?.runOnUiThread {
            (this.childFragmentManager.findFragmentById(R.id.map_detail_fragment) as SupportMapFragment)
                .getMapAsync(this)
        }
        configureView()
        return binding.root
    }

    private fun showDetails(propertyId: String?) {
        property = properties.single { property -> property.id == propertyId }

        binding.textDescription.text = property.description
        binding.surface.text = "${this.property.surface} m²"
        binding.rooms.text = property.rooms.toString()
        binding.bathrooms.text = property.bathRooms.toString()
        binding.bedrooms.text = property.bedRooms.toString()

        binding.location.text = property.address!!.toString()

        val adapter = PictureAdapter(requestManager)
        binding.picturesRecyclerView.adapter = adapter

        adapter.submitList(property.pictures)
        adapter.notifyDataSetChanged()

        if(binding.mapConstraintLayout.contentDescription != GOOGLE_MAP_FINISH_LOADING) {
            activity?.runOnUiThread {
                (this.childFragmentManager.findFragmentById(R.id.map_detail_fragment) as SupportMapFragment)
                    .getMapAsync(this)
            }
        } else {
            moveCameraToProperty()
        }

        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment

            val title = property.address!!.street + ", " +
                    property.address!!.postalCode + " " + property.address!!.city

            browseFragment.binding.toolBar.title = title
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        editItem = menu.findItem(R.id.navigation_edit)
        editItem.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_edit -> {
                val masterDetailFragment = this.parentFragment?.parentFragment as BrowseFragment
                val propertyId = property.id
                val bundle = bundleOf(Constants.FROM to arguments?.getString(Constants.FROM),
                    PROPERTY_ID to propertyId
                )
                masterDetailFragment.detail.findNavController().navigate(R.id.navigation_edit, bundle)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            editItem.isVisible = false
            onBackPressedCallback.isEnabled = false
        } else {
            initializeToolbar()
            configureView()
            onBackPressedCallback.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        this.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.setOnItemClickListener(this)
        }
    }

    private fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (resources.getBoolean(R.bool.isMasterDetail)) {
                    backPressedWhenTabletMode()
                } else {
                    backPressedWhenNormalMode()
                }
                isEnabled = false
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onItemClick(propertyId: String) {
        showDetails(propertyId = propertyId)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureView()
    }

    private fun configureView() {
        this.parentFragment?.let {
            if (resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp >= 600
                && resources.configuration.smallestScreenWidthDp < 720
            ) {
                configureTabletView()
            } else if((resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp > 720
                        && resources.configuration.orientation == ORIENTATION_LANDSCAPE)) {
                configureTabletView()
            } else if((resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp > 720
                        && resources.configuration.orientation == ORIENTATION_PORTRAIT)) {
                configureNormalView()
            }
            else if (!resources.getBoolean(R.bool.isMasterDetail)) {
                configureNormalView()
            }
        }
    }

    private fun configureNormalView() {
        val detailLayoutParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val detailFragment = this.parentFragment as NavHostFragment

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val masterWidthWeight = TypedValue()
        val detailWidthWeight = TypedValue()

        if(resources.configuration.smallestScreenWidthDp > 720) {

            resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
            detailLayoutParams.width = (screenWidth * detailWidthWeight.float).toInt()
            detailLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT


            resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)

            detailLayoutParams.leftMargin = (screenWidth * masterWidthWeight.float).toInt()
            detailFragment.requireView().layoutParams = detailLayoutParams
            detailFragment.requireView().requestLayout()
        } else {
            detailLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            detailLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            detailLayoutParams.leftMargin = 0
            detailFragment.requireView().layoutParams = detailLayoutParams
            detailFragment.requireView().requestLayout()
        }

        val infoParams = binding.infosConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
        infoParams.endToStart = ConstraintLayout.LayoutParams.UNSET
        infoParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        infoParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        binding.infosConstraintLayout.layoutParams = infoParams

        val mapParams = binding.mapConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
        mapParams.topToTop = ConstraintLayout.LayoutParams.UNSET
        mapParams.topToBottom = binding.infosConstraintLayout.id
        mapParams.startToEnd = ConstraintLayout.LayoutParams.UNSET
        mapParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID

        if(resources.configuration.smallestScreenWidthDp > 720) {
            mapParams.width = (screenWidth * detailWidthWeight.float).toInt()
            mapParams.height = (screenWidth * detailWidthWeight.float).toInt()
        } else {
            mapParams.width = screenWidth
            mapParams.height = screenWidth
        }

        mapParams.setMargins(16, 16, 16, 16)
        binding.mapConstraintLayout.layoutParams = mapParams
    }

    private fun configureTabletView() {

        val detailLayoutParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val detailFragment = this.parentFragment as NavHostFragment

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val detailWidthWeight = TypedValue()
        resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
        detailLayoutParams.width = (screenWidth * detailWidthWeight.float).toInt()
        detailLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val masterWidthWeight = TypedValue()
        resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)

        detailLayoutParams.leftMargin = (screenWidth * masterWidthWeight.float).toInt()
        detailFragment.requireView().layoutParams = detailLayoutParams
        detailFragment.requireView().requestLayout()

        val mapParams = binding.mapConstraintLayout.layoutParams as ConstraintLayout.LayoutParams

        mapParams.topToBottom = ConstraintLayout.LayoutParams.UNSET
        mapParams.topToTop = binding.textDescription.id
        mapParams.startToStart = ConstraintLayout.LayoutParams.UNSET
        mapParams.startToEnd = binding.infosConstraintLayout.id
        mapParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        mapParams.width = 250
        mapParams.height = 250
        mapParams.setMargins(0, 0, 0, 0)
        binding.mapConstraintLayout.layoutParams = mapParams

        val infoParams = binding.infosConstraintLayout.layoutParams as ConstraintLayout.LayoutParams

        infoParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
        infoParams.endToStart = binding.mapConstraintLayout.id
        infoParams.width = (screenWidth * 0.4f).toInt()
        binding.infosConstraintLayout.layoutParams = infoParams
    }



    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val title = property.address!!.street + ", " +
                    property.address!!.postalCode + " " + property.address!!.city

            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment

            browseFragment.binding.toolBar.title = title

            browseFragment.binding.toolBar.setNavigationOnClickListener {
                if (resources.getBoolean(R.bool.isMasterDetail)) {
                    backPressedWhenTabletMode()
                } else {
                    backPressedWhenNormalMode()
                }
                onBackPressedCallback.isEnabled = false
            }
        }
    }

    private fun backPressedWhenTabletMode() {
        when(arguments?.getString(Constants.FROM)) {
            MapFragment::class.java.name -> {
                (parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .navController
                    .navigate(R.id.navigation_map)
            }
        }
    }

    private fun backPressedWhenNormalMode() {
        when(arguments?.getString(Constants.FROM)) {
            ListFragment::class.java.name -> {
//                (this.parentFragment?.parentFragment as BrowseFragment)
//                        .master
//                        .navController
//                        .navigate(R.id.navigation_list)
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .master
                    .requireView()
                    .visibility = VISIBLE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .requireView()
                    .visibility = GONE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .navController
                    .navigate(R.id.navigation_map)
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.buttonContainer.visibility = VISIBLE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.listViewButton.isSelected = true
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.mapViewButton.isSelected = false
            }
            MapFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .navController
                    .navigate(R.id.navigation_map)
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .master
                    .requireView()
                    .visibility = GONE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .requireView()
                    .visibility = VISIBLE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.buttonContainer.visibility = VISIBLE
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.listViewButton.isSelected = false
                (this.parentFragment?.parentFragment as BrowseFragment)
                    .binding.mapViewButton.isSelected = true
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val options = GoogleMapOptions().liteMode(true)
        mMap.mapType = options.mapType
        binding.mapConstraintLayout.contentDescription = GOOGLE_MAP_NOT_FINISH_LOADING
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        moveCameraToProperty()
        binding.mapConstraintLayout.contentDescription = GOOGLE_MAP_FINISH_LOADING
    }

    private fun moveCameraToProperty() {
        mMap.clear()
        mMap.addMarker(MarkerOptions()
            .position(LatLng(property.address!!.latitude,
                property.address!!.longitude)
            )
        )

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(property.address!!.latitude,
                property.address!!.longitude), (DEFAULT_ZOOM + 3))

        mMap.moveCamera(cameraUpdate)
    }

    companion object {
        const val GOOGLE_MAP_NOT_FINISH_LOADING = "google_maps_not_finish_loading"
        const val GOOGLE_MAP_FINISH_LOADING = "google_maps_finish_loading"
    }
}