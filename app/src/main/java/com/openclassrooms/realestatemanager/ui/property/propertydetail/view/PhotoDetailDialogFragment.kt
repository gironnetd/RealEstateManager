package com.openclassrooms.realestatemanager.ui.property.propertydetail.view

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.R.style.AppTheme
import com.openclassrooms.realestatemanager.databinding.FragmentDialogDetailPhotoBinding
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.property.BaseDialogFragment
import java.io.File
import java.util.*

class PhotoDetailDialogFragment : BaseDialogFragment(R.layout.fragment_dialog_detail_photo) {

    private var _binding: FragmentDialogDetailPhotoBinding? = null
    val binding get() = _binding!!

    var photo: Photo? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogDetailPhotoBinding.inflate(LayoutInflater.from(context))

        return activity?.let {
            MaterialAlertDialogBuilder(ContextThemeWrapper(context, AppTheme)).run {
                setView(binding.root)
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        applyDialogDimension()
        displayDetail()
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
        show(requireParentFragment().childFragmentManager, tag)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PHOTO, photo)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            photo = it.getParcelable(PHOTO)
            displayDetail()
        }
    }

    private fun displayDetail() {
        photo?.let { photo ->
            if(photo.id.isNotEmpty() && photo.propertyId.isNotEmpty() ) {
                if(photo.bitmap != null) {
                    binding.photoImageView.setImageBitmap(photo.bitmap)
                } else {
                    val localFile = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                    if(localFile.exists()) {  binding.photoImageView.setImageURI(localFile.toUri()) }
                }
            }

            if(photo.type != PhotoType.NONE) {
                binding.labelPhotoImage.text = resources.getString(photo.type.type)
                    .replaceFirstChar {
                        if (it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else
                            it.toString()
                    }
            }
            binding.description.setText(photo.description)
            binding.description.isFocusable = false
        }
    }

    companion object {
        const val TAG = "PhotoDetailDialog"
        const val PHOTO = "photo"
    }
}