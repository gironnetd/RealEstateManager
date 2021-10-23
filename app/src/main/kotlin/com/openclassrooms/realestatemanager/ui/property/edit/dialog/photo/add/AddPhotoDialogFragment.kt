package com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.add

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.RadioButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditPhotoBinding
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.PhotoType
import com.openclassrooms.realestatemanager.models.property.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo.EditPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.util.Constants
import java.io.File
import java.io.FileOutputStream

class AddPhotoDialogFragment(private val innerContext: Context) : EditPhotoDialogFragment() {

    override var photo: Photo = Photo()
        set(value) {
            if (value != field) {
                field = value
            }
        }

    override var tmpPhoto: Photo = Photo()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        photoBinding = FragmentDialogEditPhotoBinding.inflate(LayoutInflater.from(innerContext))

        return activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {
                setView(binding.root)
                setPositiveButton(getString(R.string.add_photo)) { _, _ ->

                    parentFragment?.let { parentFragment ->
                        when (parentFragment::class.java) {
                            PropertyUpdateFragment::class.java ->
                                parentEditFragment = parentFragment as PropertyUpdateFragment
                            PropertyCreateFragment::class.java ->
                                parentEditFragment = parentFragment as PropertyCreateFragment
                        }
                    }

                    if (binding.descriptionTextInputLayout.editText?.text.toString()
                        != resources.getString(R.string.enter_a_description)
                    ) {
                        tmpPhoto.description = binding.descriptionTextInputLayout.editText?.text.toString()
                    }

                    if (!tmpPhoto.mainPhoto && binding.isMainPhoto.isChecked) {
                        parentEditFragment.newProperty.photos.singleOrNull { it.mainPhoto }?.let { photo ->
                            photo.mainPhoto = false
                        }
                        tmpPhoto.mainPhoto = true
                        parentEditFragment.newProperty.mainPhotoId = tmpPhoto.id
                    }

                    if (binding.photoImageview.drawable != null) {
                        if (parentEditFragment is PropertyUpdateFragment) {
                            val bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap

                            tmpPhoto.id = Firebase.firestore.collection(Constants.PROPERTIES_COLLECTION)
                                .document(tmpPhoto.propertyId)
                                .collection(Constants.PHOTOS_COLLECTION)
                                .document().id

                            val outputStream = FileOutputStream(
                                File(tmpPhoto.storageLocalDatabase(requireContext().cacheDir, true)), true
                            )

                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        }
                        tmpPhoto.bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap
                    }

                    tmpFile?.delete()

                    tmpPhoto.locallyCreated = true
                    parentEditFragment.newProperty.photos.add(parentEditFragment.newProperty.photos.size, tmpPhoto)

                    if (parentEditFragment.binding.noPhotos.visibility == VISIBLE) {
                        parentEditFragment.binding.noPhotos.visibility = GONE
                    }

                    with(parentEditFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter) {
                        submitList(parentEditFragment.newProperty.photos)
                    }
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
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

    override fun selectSingleChoicePhotoType(radioButton: RadioButton) {
        super.selectSingleChoicePhotoType(radioButton)

        when (radioButton.id) {
            R.id.radio_button_lounge -> { tmpPhoto.type = PhotoType.LOUNGE }
            R.id.radio_button_facade -> { tmpPhoto.type = PhotoType.FACADE }
            R.id.radio_button_kitchen -> { tmpPhoto.type = PhotoType.KITCHEN }
            R.id.radio_button_bedroom -> { tmpPhoto.type = PhotoType.BEDROOM }
            R.id.radio_button_bathroom -> { tmpPhoto.type = PhotoType.BATHROOM }
        }
    }

    override fun deletePhoto() {
        super.deletePhoto()
        tmpPhoto.bitmap = null
    }
}
