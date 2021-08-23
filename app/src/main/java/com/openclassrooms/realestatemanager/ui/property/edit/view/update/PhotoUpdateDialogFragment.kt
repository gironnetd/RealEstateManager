package com.openclassrooms.realestatemanager.ui.property.edit.view.update

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogUpdatePhotoBinding
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.property.BaseDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import java.io.File
import java.io.FileOutputStream

class PhotoUpdateDialogFragment : BaseDialogFragment(R.layout.fragment_dialog_update_photo) {

    private var _binding: FragmentDialogUpdatePhotoBinding? = null
    val binding get() = _binding!!

    var photo: Photo? = null

    var latestTmpUri: Uri? = null
    var tmpFile: File? = null

    var registry: ActivityResultRegistry? = null
    private lateinit var selectImageFromGalleryResult: ActivityResultLauncher<String>
    private lateinit var takeImageResult: ActivityResultLauncher<Void?>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogUpdatePhotoBinding.inflate(LayoutInflater.from(context))
        return activity?.let {
            MaterialAlertDialogBuilder(ContextThemeWrapper(context, R.style.AppTheme)).run {
                setView(binding.root)

                setPositiveButton(getString(R.string.update_photo_detail)) { _, _ ->
                    val propertyEditFragment: PropertyEditFragment = parentFragment as PropertyEditFragment

                    photo?.let { photo ->
                        if(binding.descriptionTextInputLayout.editText?.text.toString() != resources.getString(R.string.enter_a_description)) {
                            photo.description = binding.descriptionTextInputLayout.editText?.text.toString()
                        }

                        if(!photo.mainPhoto && binding.isMainPhoto.isChecked) {
                            propertyEditFragment.newProperty.photos.singleOrNull { it.mainPhoto }?.let {
                                it.mainPhoto = false
                            }
                            photo.mainPhoto = true
                            propertyEditFragment.newProperty.mainPhotoId = photo.id
                        }

                        if(binding.photoImageview.drawable != null) {
                            val bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap

                            val file = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                            file.delete()

                            val outputStream = FileOutputStream(
                                File(photo.storageLocalDatabase(requireContext().cacheDir, true)), true)

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
                setNeutralButton(getString(R.string.cancel)) { _, _ ->}
                setNegativeButton(getString(R.string.delete_photo)) { _, _ ->

                    photo?.let { photo ->
                        if (photo.mainPhoto) {
                            Toast.makeText(requireContext(), R.string.cannot_delete_photo, Toast.LENGTH_LONG).show()
                            return@setNegativeButton
                        }

                        photo.locallyDeleted = true

                        val propertyEditFragment: PropertyEditFragment = parentFragment as PropertyEditFragment
                        with(propertyEditFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter) {
                            submitList(propertyEditFragment.newProperty.photos.filter { photo -> !photo.locallyDeleted })
                        }
                    }

                }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        applyDialogDimension()
        initInteraction()
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
        show(requireParentFragment().childFragmentManager, tag)
    }

    private fun initInteraction() {
        with(binding) {
            photo?.let { photo ->
                if(photo.bitmap != null) {
                    photoImageview.setImageBitmap(photo.bitmap)

                    if (photoImageview.visibility == View.INVISIBLE) { photoImageview.visibility = View.VISIBLE }
                    if (deletePhoto.visibility == View.GONE) { deletePhoto.visibility = View.VISIBLE }
                } else if (photo.bitmap == null && photo.id.isNotEmpty() && photo.propertyId.isNotEmpty()) {
                    val localFile = File(photo.storageLocalDatabase(requireContext().cacheDir, true))
                    if(localFile.exists()) {
                        photoImageview.setImageURI(localFile.toUri())
                    }

                    if (photoImageview.visibility == View.INVISIBLE) { photoImageview.visibility = View.VISIBLE }
                    if (deletePhoto.visibility == View.GONE) { deletePhoto.visibility = View.VISIBLE }
                }



                descriptionTextInputLayout.editText?.imeOptions = EditorInfo.IME_ACTION_DONE
                descriptionTextInputLayout.editText?.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)

                descriptionTextInputLayout.editText?.setText(photo.description)

                descriptionTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && descriptionTextInputLayout.editText?.text.toString()
                        == resources.getString(R.string.enter_a_description)
                    ) {
                        descriptionTextInputLayout.editText?.text?.clear()
                    } else if (!hasFocus && descriptionTextInputLayout.editText?.text.toString()
                            .isEmpty()
                    ) {
                        descriptionTextInputLayout.editText?.setText(R.string.enter_a_description)
                    }
                }

                if(photo.mainPhoto) {
                    isMainPhoto.isChecked = true
                    isMainPhoto.isClickable = false
                }

                when(photo.type.type) {
                    R.string.photo_type_lounge -> { radioButtonLounge.isChecked = true }
                    R.string.photo_type_facade -> { radioButtonFacade.isChecked = true }
                    R.string.photo_type_kitchen -> { radioButtonKitchen.isChecked = true }
                    R.string.photo_type_bedroom -> { radioButtonBedroom.isChecked = true }
                    R.string.photo_type_bathroom -> { radioButtonBathroom.isChecked = true }
                }

                radioButtonLounge.setOnClickListener { selectSingleChoicePhotoType(it as RadioButton) }
                radioButtonFacade.setOnClickListener { selectSingleChoicePhotoType(it as RadioButton) }
                radioButtonKitchen.setOnClickListener { selectSingleChoicePhotoType(it as RadioButton) }
                radioButtonBedroom.setOnClickListener { selectSingleChoicePhotoType(it as RadioButton) }
                radioButtonBathroom.setOnClickListener { selectSingleChoicePhotoType(it as RadioButton) }

                latestTmpUri?.let { displayPhoto() }
                deletePhoto.setOnClickListener { deletePhoto() }

                selectImageFromGalleryResult = registerForActivityResult(
                    ActivityResultContracts.GetContent(),
                    registry ?: requireActivity().activityResultRegistry
                ) { uri: Uri? ->
                    uri?.let {
                        latestTmpUri = uri
                        tmpFile?.delete()
                        tmpFile = File(requireContext().cacheDir, latestTmpUri!!.lastPathSegment!!)
                        if(binding.deletePhoto.visibility == View.GONE) { binding.deletePhoto.visibility =
                            View.VISIBLE
                        }
                        displayPhoto()
                    }
                }

                takeImageResult = registerForActivityResult(
                    ActivityResultContracts.TakePicturePreview(),
                    registry ?: requireActivity().activityResultRegistry
                ) { bitmap ->
                    try {
                        val outputStream = FileOutputStream(
                            File(requireContext().cacheDir, latestTmpUri?.lastPathSegment!!), true
                        )

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        outputStream.close()
                        if(binding.deletePhoto.visibility == View.GONE) { binding.deletePhoto.visibility =
                            View.VISIBLE
                        }
                        displayPhoto()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                takePhoto.setOnClickListener { takePhoto() }
                selectPhotoFromGallery.setOnClickListener { selectImageFromGallery() }
            }
        }
    }

    private fun displayPhoto() {
        if(binding.addPhotoTextview.visibility == View.VISIBLE) { binding.addPhotoTextview.visibility =
            View.GONE
        }
        if(binding.photoImageview.visibility == View.INVISIBLE) { binding.photoImageview.visibility =
            View.VISIBLE
        }

        binding.photoImageview.setImageURI(latestTmpUri)

    }

    private fun selectImageFromGallery() {
        selectImageFromGalleryResult.launch("image/*")
    }

    private fun takePhoto() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takeImageResult.launch()
        }
    }

    private fun getTmpFileUri(): Uri {
        tmpFile?.delete()
        tmpFile = File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return tmpFile!!.toUri()
    }

    private fun selectSingleChoicePhotoType(radioButton: RadioButton) {
        clearRadioButtons()
        radioButton.isChecked = true

        when(radioButton.id) {
            R.id.radio_button_lounge -> { photo?.type = PhotoType.LOUNGE }
            R.id.radio_button_facade -> { photo?.type = PhotoType.FACADE }
            R.id.radio_button_kitchen -> { photo?.type = PhotoType.KITCHEN }
            R.id.radio_button_bedroom -> { photo?.type = PhotoType.BEDROOM }
            R.id.radio_button_bathroom -> { photo?.type = PhotoType.BATHROOM }
        }
    }

    private fun clearRadioButtons() {
        with(binding) {
            radioButtonLounge.isChecked = false
            radioButtonFacade.isChecked = false
            radioButtonKitchen.isChecked = false
            radioButtonBedroom.isChecked = false
            radioButtonBathroom.isChecked = false
        }
    }

    private fun deletePhoto() {
        with(binding) {
            photoImageview.setImageURI(null)
            photoImageview.setImageResource(0)
            photoImageview.setImageDrawable(null)
            tmpFile?.delete()
            photoImageview.visibility = View.INVISIBLE
            deletePhoto.visibility = View.GONE
            addPhotoTextview.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "PhotoUpdateDialog"
    }
}