package com.openclassrooms.realestatemanager.ui.property.browse.detail

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.util.Constants
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
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private var _binding: FragmentDetailBinding? = null
    val binding get() = _binding!!

    lateinit var property: Property

    private lateinit var editItem: MenuItem

    lateinit var mMap: GoogleMap

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        property = properties.single { property -> property.id == arguments?.getString(Constants.PROPERTY_ID) }
        setHasOptionsMenu(true)

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

        activity?.runOnUiThread {
            (this.childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment)
                    .getMapAsync(this)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        editItem = menu.findItem(R.id.navigation_edit)
        editItem.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_edit -> {
                when(this.parentFragment?.parentFragment?.javaClass?.name) {

                    BrowseMasterFragment::class.java.name -> {
                        val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment
                        val propertyId = property.id
                        val action = DetailFragmentDirections.navigationEditAction(
                                from = arguments?.getString(Constants.FROM),
                                propertyId = propertyId
                        )
                        masterFragment.master.findNavController().navigate(action)
                    }

                    BrowseMasterDetailFragment::class.java.name -> {
                        val masterDetailFragment = this.parentFragment?.parentFragment as BrowseMasterDetailFragment
                        val propertyId = property.id
                        val bundle = bundleOf(Constants.FROM to arguments?.getString(Constants.FROM),
                                Constants.PROPERTY_ID to propertyId
                        )
                        masterDetailFragment.detail.findNavController().navigate(R.id.navigation_edit, bundle)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(hidden) {
            editItem.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        when(this.parentFragment?.parentFragment?.javaClass?.name) {

            BrowseMasterFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment).binding.buttonContainer.visibility = GONE

                activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPressedWhenNormalMode()
                    }
                })
            }

            BrowseMasterDetailFragment::class.java.name -> {
                activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPressedWhenTabletMode()
                    }
                })
            }
        }
    }

    override fun initializeToolbar() {

        val title = property.address!!.street + ", " +
                property.address!!.postalCode + " " + property.address!!.city

        when(this.parentFragment?.parentFragment?.javaClass?.name) {

            BrowseMasterFragment::class.java.name -> {
                val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment

                masterFragment.binding.toolBar.title = title
                masterFragment.binding.toolBar.setNavigationOnClickListener {
                    backPressedWhenNormalMode()
                }
            }

            BrowseMasterDetailFragment::class.java.name -> {
                val masterDetailFragment =  this.parentFragment?.parentFragment as BrowseMasterDetailFragment

                masterDetailFragment.binding.toolBar.title = title

                masterDetailFragment.binding.toolBar.setNavigationOnClickListener {
                    backPressedWhenTabletMode()
                }
            }
        }
    }

    private fun backPressedWhenTabletMode() {
        when(arguments?.getString(Constants.FROM)) {
            MapFragment::class.java.name -> {
                (parentFragment?.parentFragment as BrowseMasterDetailFragment)
                        .detail
                        .navController
                        .navigate(R.id.navigation_map)
            }
        }
    }

    private fun backPressedWhenNormalMode() {
        when(arguments?.getString(Constants.FROM)) {
            ListFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment)
                        .master
                        .navController
                        .navigate(R.id.navigation_list)
            }
            MapFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment)
                        .master
                        .navController
                        .navigate(R.id.navigation_map)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val options = GoogleMapOptions().liteMode(true)
        mMap.mapType = options.mapType
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
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
}