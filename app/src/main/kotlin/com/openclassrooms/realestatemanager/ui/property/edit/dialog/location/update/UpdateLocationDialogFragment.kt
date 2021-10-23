package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.update

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditLocationBinding
import com.openclassrooms.realestatemanager.models.property.Address
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.SearchLocationAdapter

class UpdateLocationDialogFragment(private val innerContext: Context, initialAddress: Address) :
    EditLocationDialogFragment(innerContext), SearchLocationAdapter.SearchListener {

    private var supportMapFragment: SupportMapFragment? = null

    override var address = initialAddress
        set(value) {
            if (value != field) {
                field = value
            }
            showAddress(field)
            release()
        }

    override var tmpAddress = address.deepCopy()

    interface UpdateLocationListener {
        fun onUpdateLocationClick()
    }

    private var callBack: UpdateLocationListener? = null

    fun setCallBack(listener: UpdateLocationListener) { callBack = listener }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        locationBinding = FragmentDialogEditLocationBinding.inflate(LayoutInflater.from(innerContext))

        alertDialog = activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)

                setPositiveButton(getString(R.string.update_location)) { _, _ ->
                    (parentFragment as PropertyEditFragment).newProperty.address = tmpAddress
                    callBack?.onUpdateLocationClick()
                }

                setNeutralButton(getString(R.string.cancel)) { _, _ -> }

                it.runOnUiThread {
                    supportMapFragment = (
                        it.supportFragmentManager.findFragmentById(R.id.map_update_dialog_fragment)
                            as SupportMapFragment
                        )
                    supportMapFragment?.getMapAsync(this@UpdateLocationDialogFragment)
                }
                create()
            }
        } ?: throw error("Activity cannot be null")
        return alertDialog
    }

    override fun dismiss() {
        super.dismiss()
        if (supportMapFragment != null) {
            requireActivity().supportFragmentManager.beginTransaction().remove(supportMapFragment!!).commit()
            requireActivity().supportFragmentManager.executePendingTransactions()
            supportMapFragment = null
        }
    }

    override fun release() {
        super.release()
        tmpAddress = address
    }
}
