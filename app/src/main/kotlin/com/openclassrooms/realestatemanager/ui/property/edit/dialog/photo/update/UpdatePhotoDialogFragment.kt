package com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.update

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditPhotoBinding
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.PhotoType
import com.openclassrooms.realestatemanager.models.property.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.EditPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import java.io.File
import java.io.FileOutputStream

class UpdatePhotoDialogFragment(private val innerContext: Context, initialPhoto: Photo) : EditPhotoDialogFragment() {

    override var photo = initialPhoto
        set(value) {
            if (value != field) {
                field = value
            }
        }

    override var tmpPhoto = photo.deepCopy()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        photoBinding = FragmentDialogEditPhotoBinding.inflate(LayoutInflater.from(innerContext))
        return activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)

                setPositiveButton(getString(R.string.update_photo_detail)) { _, _ ->
                    val propertyEditFragment: PropertyEditFragment = parentFragment as PropertyEditFragment

                    photo.let { photo ->
                        if (binding.descriptionTextInputLayout.editText?.text.toString()
                            != resources.getString(R.string.enter_a_description)
                        ) {
                            photo.description = binding.descriptionTextInputLayout.editText?.text.toString()
                        }

                        if (!photo.mainPhoto && binding.isMainPhoto.isChecked) {
                            propertyEditFragment.newProperty.photos.singleOrNull { it.mainPhoto }?.let {
                                it.mainPhoto = false
                            }
                            photo.mainPhoto = true
                            propertyEditFragment.newProperty.mainPhotoId = photo.id
                        }

                        if (binding.photoImageview.drawable != null) {
                            val bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap

                            val file = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                            file.delete()

                            val outputStream = FileOutputStream(
                                File(photo.storageLocalDatabase(requireContext().cacheDir, true)), true
                            )

                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                            photo.bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap
                        } else {
                            val file = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                            file.delete()
                            photo.bitmap = null
                        }

                        tmpFile?.delete()
                        photo.locallyUpdated = true

                        with(propertyEditFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter) {
                            submitList(propertyEditFragment.newProperty.photos)
                        }
                    }
                }
                setNeutralButton(getString(R.string.cancel)) { _, _ -> }
                setNegativeButton(getString(R.string.delete_photo)) { _, _ ->

                    photo.let { photo ->
                        if (photo.mainPhoto) {
                            Toast.makeText(requireContext(), R.string.cannot_delete_photo, Toast.LENGTH_LONG).show()
                            return@setNegativeButton
                        }

                        photo.locallyDeleted = true

                        val propertyEditFragment: PropertyEditFragment = parentFragment as PropertyEditFragment
                        with(propertyEditFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter) {
                            submitList(
                                propertyEditFragment.newProperty.photos.filter { photo ->
                                    !photo.locallyDeleted
                                }
                            )
                        }
                    }
                }
                create()
            }
        } ?: throw error("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        applyDialogDimension()
        initInteraction()
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            tmpPhoto = it.getParcelable(PHOTO)!!
            initInteraction()
        }
    }

    override fun initInteraction() {
        super.initInteraction()
        with(binding) {
            tmpPhoto.let { photo ->
                if (photo.bitmap != null) {
                    photoImageview.setImageBitmap(photo.bitmap)

                    if (photoImageview.visibility == View.INVISIBLE) { photoImageview.visibility = View.VISIBLE }
                    if (deletePhoto.visibility == View.GONE) { deletePhoto.visibility = View.VISIBLE }
                } else if (photo.bitmap == null && photo.id.isNotEmpty() && photo.propertyId.isNotEmpty()) {
                    val localFile = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                    if (localFile.exists()) {
                        photoImageview.setImageURI(localFile.toUri())
                    }

                    if (photoImageview.visibility == View.INVISIBLE) { photoImageview.visibility = View.VISIBLE }
                    if (deletePhoto.visibility == View.GONE) { deletePhoto.visibility = View.VISIBLE }
                }

                if (photo.mainPhoto) {
                    isMainPhoto.isChecked = true
                    isMainPhoto.isClickable = false
                }

                when (photo.type.type) {
                    R.string.photo_type_lounge -> { radioButtonLounge.isChecked = true }
                    R.string.photo_type_facade -> { radioButtonFacade.isChecked = true }
                    R.string.photo_type_kitchen -> { radioButtonKitchen.isChecked = true }
                    R.string.photo_type_bedroom -> { radioButtonBedroom.isChecked = true }
                    R.string.photo_type_bathroom -> { radioButtonBathroom.isChecked = true }
                }

                descriptionTextInputLayout.editText?.setText(photo.description)
            }
        }
    }

    override fun selectSingleChoicePhotoType(radioButton: RadioButton) {
        super.selectSingleChoicePhotoType(radioButton)

        when (radioButton.id) {
            R.id.radio_button_lounge -> { photo.type = PhotoType.LOUNGE }
            R.id.radio_button_facade -> { photo.type = PhotoType.FACADE }
            R.id.radio_button_kitchen -> { photo.type = PhotoType.KITCHEN }
            R.id.radio_button_bedroom -> { photo.type = PhotoType.BEDROOM }
            R.id.radio_button_bathroom -> { photo.type = PhotoType.BATHROOM }
        }
    }
}
