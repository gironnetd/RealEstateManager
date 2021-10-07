package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.update

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogUpdateLocationBinding
import com.openclassrooms.realestatemanager.extensions.setHeightPercent
import com.openclassrooms.realestatemanager.extensions.setWidthPercent
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.SearchLocationAdapter
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.EspressoIdlingResource
import timber.log.Timber
import java.util.*

class UpdateLocationDialogFragment(private val innerContext: Context, initialAddress: Address) : BaseDialogFragment(R.layout.fragment_dialog_update_location),
    OnMapReadyCallback, OnMapLoadedCallback, SearchLocationAdapter.SearchListener {

    private var _binding: FragmentDialogUpdateLocationBinding? = null
    val binding get() = _binding!!

    lateinit var alertDialog: AlertDialog
    private lateinit var mMap: GoogleMap

    interface UpdateLocationListener {
        fun onUpdateLocationClick()
    }

    private var callBack: UpdateLocationListener? = null

    fun setCallBack(listener: UpdateLocationListener) { callBack = listener }

    private var supportMapFragment: SupportMapFragment? = null

    var address: Address = initialAddress
        set(value) {
            if(value != field) {
                field = value
            }
            showAddress(field)
            release()
        }

    var tmpAddress: Address = address.deepCopy()

    private val token: AutocompleteSessionToken by lazy {
        AutocompleteSessionToken.newInstance()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogUpdateLocationBinding.inflate(LayoutInflater.from(innerContext))

        alertDialog = activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)

                setPositiveButton(getString(R.string.update_location)) { _, _ ->
                    (parentFragment as PropertyEditFragment).newProperty.address = tmpAddress
                    callBack?.onUpdateLocationClick()
                }

                setNeutralButton(getString(R.string.cancel)) { _, _ ->}

                it.runOnUiThread {
                    supportMapFragment = (it.supportFragmentManager.findFragmentById(R.id.map_update_dialog_fragment) as SupportMapFragment)
                    supportMapFragment?.getMapAsync(this@UpdateLocationDialogFragment)
                }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
        return alertDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setWidthPercent(100)

        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setHeightPercent(100)
        }
        // Get the SearchView and set the searchable configuration
        val searchManager = innerContext.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.locationSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

        binding.locationSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                EspressoIdlingResource.increment()
                val requestBuilder = FindAutocompletePredictionsRequest
                    .builder()
                    .setTypeFilter(TypeFilter.ADDRESS)
                    .setQuery(query)

                val task: Task<FindAutocompletePredictionsResponse> =
                    (requireActivity() as MainActivity).placesClient.findAutocompletePredictions(requestBuilder.build())

                task.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val predictions = response.autocompletePredictions
                    val primaryText: MutableList<SpannableString> = ArrayList()
                    for (prediction in predictions) {
                        primaryText.add(prediction.getFullText(StyleSpan(Typeface.BOLD)))
                    }

                    with(binding) {
                        resultSearchLocation.visibility = View.VISIBLE

                        binding.resultSearchLocation.apply {
                            layoutManager = LinearLayoutManager(innerContext)
                            val recyclerAdapter = SearchLocationAdapter(innerContext).apply {
                                setCallBack(this@UpdateLocationDialogFragment)
                            }
                            recyclerAdapter.submitList(predictions)
                            adapter = recyclerAdapter
                            EspressoIdlingResource.decrement()
                        }
                    }
                }.addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Timber.e("Place not found: " + exception.statusCode)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val closeButton: ImageView = binding.locationSearchView.findViewById(R.id.search_close_btn)
        closeButton.setOnClickListener {
            showAddress(address)
            release()
        }
        return binding.root
    }

    private fun release() {
        with(binding) {
            resultSearchLocation.visibility = View.GONE
            streetTextInputLayout.visibility = View.VISIBLE
            cityTextInputLayout.visibility = View.VISIBLE
            postalCodeTextInputLayout.visibility = View.VISIBLE
            countryTextInputLayout.visibility = View.VISIBLE
            stateTextInputLayout.visibility = View.VISIBLE

            with((locationSearchView.findViewById(R.id.search_src_text) as EditText)) {
                text.clear()
                clearFocus()
            }
            tmpAddress = address
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
        show(requireParentFragment().childFragmentManager, TAG)
    }

    override fun dismiss() {
        super.dismiss()
        if(supportMapFragment != null) {
            requireActivity().supportFragmentManager.beginTransaction().remove(supportMapFragment!!).commit()
            requireActivity().supportFragmentManager.executePendingTransactions()
            supportMapFragment = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ADDRESS, tmpAddress)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            tmpAddress = it.getParcelable(ADDRESS)!!
            //address = tmpAddress
            showAddress(tmpAddress)
        }
    }

    private fun showAddress(address: Address) {
        mMap.clear()

        if(!address.latitude.equals(0.0) && !address.longitude.equals(0.0)) {

            mMap.addMarker(
                MarkerOptions()
                .position(LatLng(address.latitude, address.longitude))
                .icon(BitmapUtil.bitmapDescriptorFromVector(innerContext, R.drawable.ic_marker_selected))
                .title(address.street)
            )

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(address.latitude, address.longitude),
                (BaseMapFragment.DEFAULT_ZOOM + 3)
            )

            mMap.moveCamera(cameraUpdate)
        }

        with(binding) {
            street.setText(address.street)
            city.setText(address.city)
            postalCode.setText(address.postalCode)
            country.setText(address.country)
            state.setText(address.state)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        /*
        Deprecation notice: In a future release, indoor will no longer be supported on satellite,
        hybrid or terrain type maps. Even where indoor is not supported, isIndoorEnabled()
        will continue to return the value that has been set via setIndoorEnabled(),
        as it does now. By default, setIndoorEnabled is 'true'.
        The API release notes (https://developers.google.com/maps/documentation/android-api/releases)
        will let you know when indoor support becomes unavailable on those map types.
        */
        mMap = googleMap
        mMap.isIndoorEnabled = false

        val options = GoogleMapOptions().liteMode(true)
        mMap.mapType = options.mapType
        mMap.setContentDescription(UPDATE_LOCATION_MAP_NOT_FINISH_LOADING)
        showAddress(tmpAddress)
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        mMap.setContentDescription(UPDATE_LOCATION_MAP_FINISH_LOADING)
    }

    override fun onSearchItemClick(placeId: String) {
        // Specify the fields to return.
        binding.resultSearchLocation.visibility = View.GONE

        EspressoIdlingResource.increment()
        val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS )

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        (requireActivity() as MainActivity).placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                //Log.i(TAG, "Place found: ${place.name}")

                with(place) {
                    latLng?.let { latLng ->
                        mMap.clear()

                        mMap.addMarker(
                            MarkerOptions()
                            .position(
                                LatLng(latLng.latitude, latLng.longitude)
                            ).icon(BitmapUtil.bitmapDescriptorFromVector(innerContext, R.drawable.ic_marker_selected))
                        )

                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(latLng.latitude, latLng.longitude), 18f)

                        mMap.animateCamera(cameraUpdate)

                        tmpAddress.latitude = latLng.latitude
                        tmpAddress.longitude = latLng.longitude

                        with(binding) {
                            street.setText(none)
                            city.setText(none)
                            postalCode.setText(none)
                            country.setText(none)
                            state.setText(none)
                        }

                        addressComponents?.let { addressComponents ->
                            addressComponents.asList().onEach { addressComponent ->
                                when {
                                    addressComponent.types.contains(
                                        Place.Type.COUNTRY.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        binding.country.setText(addressComponent.name)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.LOCALITY.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        binding.city.setText(addressComponent.name)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.POSTAL_CODE.name.lowercase(
                                            Locale.getDefault())) ||
                                            addressComponent.types.contains(
                                                Place.Type.POSTAL_CODE_PREFIX.name.lowercase(
                                                    Locale.getDefault()))
                                    -> {
                                        binding.postalCode.setText(addressComponent.shortName)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.POSTAL_TOWN.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        binding.city.setText(addressComponent.name)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.STREET_ADDRESS.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        if(binding.street.text.toString() == none) {
                                            binding.street.text?.clear()
                                        }
                                        binding.street.setText(addressComponent.name)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.ROUTE.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        if(binding.street.text.toString() == none) {
                                            binding.street.text?.clear()
                                        }
                                        binding.street.append(addressComponent.name)
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.STREET_NUMBER.name.lowercase(
                                            Locale.getDefault()))
                                    -> {
                                        if(binding.street.text.toString() == none) {
                                            binding.street.text?.clear()
                                        }
                                        binding.street.setText(addressComponent.name + " ")
                                    }

                                    addressComponent.types.contains(
                                        Place.Type.ADMINISTRATIVE_AREA_LEVEL_1.name.lowercase(
                                            Locale.getDefault())) -> {
                                        binding.state.setText(addressComponent.name)
                                        binding.postalCode.setText(addressComponent.shortName)
                                    }
                                }
                            }
                        }
                        EspressoIdlingResource.decrement()

                        tmpAddress.street = binding.street.text.toString()
                        tmpAddress.city = binding.city.text.toString()
                        tmpAddress.postalCode = binding.postalCode.text.toString()
                        tmpAddress.country = binding.country.text.toString()
                        tmpAddress.state = binding.state.text.toString()
                    }
                }
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Timber.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }

    companion object {
        const val TAG = "LocationUpdateDialog"
        const val ADDRESS = "address"

        // constant variable to perform ui automator testing
        const val UPDATE_LOCATION_MAP_NOT_FINISH_LOADING = "update_location_map_not_finish_loading"
        const val UPDATE_LOCATION_MAP_FINISH_LOADING = "update_location_map_finish_loading"
    }
}