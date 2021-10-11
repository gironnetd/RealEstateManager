package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.add

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.gms.maps.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditLocationBinding
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.SearchLocationAdapter
import java.util.*

class AddLocationDialogFragment(private val innerContext: Context)
    : EditLocationDialogFragment(innerContext), SearchLocationAdapter.SearchListener {

    interface AddLocationListener {
        fun onAddLocationClick()
    }

    private var callBack: AddLocationListener? = null

    fun setCallBack(listener: AddLocationListener) { callBack = listener }

    private var supportMapFragment: SupportMapFragment? = null

    override var address: Address = Address()
        set(value) {
            if(value != field) {
                field = value
            }
            release()
            showAddress(field)
        }

    override var tmpAddress = Address()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogEditLocationBinding.inflate(LayoutInflater.from(innerContext))

        alertDialog = activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)

                setPositiveButton(getString(R.string.create_location)) { _, _ ->
                    (parentFragment as PropertyEditFragment).newProperty.address = tmpAddress
                    callBack?.onAddLocationClick()
                }

                setNeutralButton(getString(R.string.cancel)) { _, _ ->}

                it.runOnUiThread {
                    supportMapFragment = (it.supportFragmentManager.findFragmentById(R.id.map_update_dialog_fragment) as SupportMapFragment)
                    supportMapFragment?.getMapAsync(this@AddLocationDialogFragment)
                }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
        return alertDialog
    }

    override fun dismiss() {
        super.dismiss()
        if(supportMapFragment != null) {
            requireActivity().supportFragmentManager.beginTransaction().remove(supportMapFragment!!).commit()
            requireActivity().supportFragmentManager.executePendingTransactions()
            supportMapFragment = null
        }
    }

    override fun release() {
        super.release()
        with(binding) {
            street.setText(none)
            city.setText(none)
            postalCode.setText(none)
            country.setText(none)
            state.setText(none)
        }
        tmpAddress = Address()
    }
}