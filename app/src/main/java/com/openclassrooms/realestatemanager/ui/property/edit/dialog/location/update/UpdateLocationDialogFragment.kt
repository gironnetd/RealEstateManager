package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.update

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogUpdateLocationBinding
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.SearchLocationAdapter
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment

class UpdateLocationDialogFragment(private val innerContext: Context, initialAddress: Address) : BaseDialogFragment(R.layout.fragment_dialog_update_location),
    OnMapReadyCallback, OnMapLoadedCallback, SearchLocationAdapter.SearchListener {

    private var _binding: FragmentDialogUpdateLocationBinding? = null
    val binding get() = _binding!!

    lateinit var alertDialog: AlertDialog

    interface UpdateLocationListener {
        fun onUpdateLocationClick()
    }

    private var callBack: UpdateLocationListener? = null

    fun setCallBack(listener: UpdateLocationListener) { callBack = listener }

    var address: Address = initialAddress
    var tmpAddress: Address = address.deepCopy()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogUpdateLocationBinding.inflate(LayoutInflater.from(innerContext))

        alertDialog = activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
        return alertDialog
    }



    override fun onMapReady(googleMap: GoogleMap) {}

    override fun onMapLoaded() {}

    override fun onSearchItemClick(placeId: String) {}

    companion object {
        const val TAG = "LocationUpdateDialog"
        const val ADDRESS = "address"

        // constant variable to perform ui automator testing
        const val UPDATE_LOCATION_MAP_NOT_FINISH_LOADING = "update_location_map_not_finish_loading"
        const val UPDATE_LOCATION_MAP_FINISH_LOADING = "update_location_map_finish_loading"
    }
}