package com.openclassrooms.realestatemanager.ui.property.propertydetail.dialog.location

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogDetailLocationBinding
import com.openclassrooms.realestatemanager.extensions.setHeightPercent
import com.openclassrooms.realestatemanager.extensions.setWidthPercent
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment
import com.openclassrooms.realestatemanager.util.BitmapUtil

class DetailLocationDialogFragment(private val innerContext: Context, var property: Property) :
    BaseDialogFragment(R.layout.fragment_dialog_detail_location),
    OnMapReadyCallback {

    private var _binding: FragmentDialogDetailLocationBinding? = null
    val binding get() = _binding!!

    lateinit var alertDialog: AlertDialog
    private lateinit var mMap: GoogleMap

    private var supportMapFragment: SupportMapFragment? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogDetailLocationBinding.inflate(LayoutInflater.from(innerContext))

        alertDialog = activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)
                it.runOnUiThread {
                    supportMapFragment = (
                        it.supportFragmentManager.findFragmentById(R.id.map_detail_dialog_fragment)
                            as SupportMapFragment
                        )
                    supportMapFragment?.getMapAsync(this@DetailLocationDialogFragment)
                }
                create()
            }
        } ?: throw error("Activity cannot be null")
        return alertDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setHeightPercent(portraitHeightPercentFragment)
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setWidthPercent(landscapeWidthPercentFragment)
        }
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
        show(requireParentFragment().childFragmentManager, TAG)
    }

    override fun dismiss() {
        super.dismiss()
        if (supportMapFragment != null) {
            requireActivity().supportFragmentManager.beginTransaction().remove(supportMapFragment!!).commit()
            requireActivity().supportFragmentManager.executePendingTransactions()
            supportMapFragment = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PROPERTY, property)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            property = it.getParcelable(PROPERTY)!!
            displayProperty()
        }
    }

    private fun displayProperty() {
        mMap.clear()

        if (!property.address.latitude.equals(0.0) && !property.address.longitude.equals(0.0)) {
            mMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            property.address.latitude,
                            property.address.longitude
                        )
                    ).icon(BitmapUtil.bitmapDescriptorFromVector(innerContext, R.drawable.ic_marker_selected))
            )

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    property.address.latitude,
                    property.address.longitude
                ),
                (DEFAULT_ZOOM)
            )

            mMap.moveCamera(cameraUpdate)
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
        displayProperty()
    }

    companion object {
        const val TAG = "LocationDetailDialog"
        const val PROPERTY = "property"

        const val DEFAULT_ZOOM = 18f
        const val portraitHeightPercentFragment = 60
        const val landscapeWidthPercentFragment = 80
    }
}
