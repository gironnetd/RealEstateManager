package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.add

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditLocationBinding
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment

class AddLocationDialogFragment(private val innerContext: Context)
    : BaseDialogFragment(R.layout.fragment_dialog_edit_location) {

    private var _binding: FragmentDialogEditLocationBinding? = null
    val binding get() = _binding!!

    var tmpAddress: Address = Address()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogEditLocationBinding.inflate(LayoutInflater.from(innerContext))

        return activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "LocationAddDialog"
        const val ADDRESS = "address"

        // constant variable to perform ui automator testing
        const val ADD_LOCATION_MAP_NOT_FINISH_LOADING = "add_location_map_not_finish_loading"
        const val ADD_LOCATION_MAP_FINISH_LOADING = "add_location_map_finish_loading"
    }
}