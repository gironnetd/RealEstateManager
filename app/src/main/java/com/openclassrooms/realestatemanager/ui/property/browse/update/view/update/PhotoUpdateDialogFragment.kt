package com.openclassrooms.realestatemanager.ui.property.browse.update.view.update

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogUpdatePhotoBinding
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.ui.property.BaseDialogFragment
import java.io.File

class PhotoUpdateDialogFragment : BaseDialogFragment(R.layout.fragment_dialog_update_photo) {

    private var _binding: FragmentDialogUpdatePhotoBinding? = null
    val binding get() = _binding!!

    var photo: Photo? = null

    var latestTmpUri: Uri? = null
    var tmpFile: File? = null

    var registry: ActivityResultRegistry? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogUpdatePhotoBinding.inflate(LayoutInflater.from(context))
        return activity?.let {
            MaterialAlertDialogBuilder(ContextThemeWrapper(context, R.style.AppTheme)).run {
                setView(binding.root)
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "PhotoUpdateDialog"
    }
}