package com.openclassrooms.realestatemanager.ui.property.edit.dialog.photo

import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.net.toUri
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogEditPhotoBinding
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

open class EditPhotoDialogFragment : BaseDialogFragment(R.layout.fragment_dialog_edit_photo) {

    var photoBinding: FragmentDialogEditPhotoBinding? = null
    val binding get() = photoBinding!!

    open lateinit var photo: Photo
    open lateinit var tmpPhoto: Photo

    var latestTmpUri: Uri? = null
    var tmpFile: File? = null

    var registry: ActivityResultRegistry? = null
    lateinit var selectImageFromGalleryResult: ActivityResultLauncher<String>
    lateinit var takeImageResult: ActivityResultLauncher<Void?>

    lateinit var parentEditFragment: PropertyEditFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        applyDialogDimension()
        initInteraction()
        return binding.root
    }

    open fun initInteraction() {
        with(binding) {
            descriptionTextInputLayout.editText?.imeOptions = EditorInfo.IME_ACTION_DONE
            descriptionTextInputLayout.editText?.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)

            descriptionTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && descriptionTextInputLayout.editText?.text.toString()
                    == resources.getString(R.string.enter_a_description)
                ) {
                    descriptionTextInputLayout.editText?.text?.clear()
                } else if (!hasFocus && descriptionTextInputLayout.editText?.text.toString().isEmpty()) {
                    descriptionTextInputLayout.editText?.setText(R.string.enter_a_description)
                }
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
                    if (binding.deletePhoto.visibility == View.GONE) {
                        binding.deletePhoto.visibility =
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
                    if (binding.deletePhoto.visibility == View.GONE) {
                        binding.deletePhoto.visibility =
                            View.VISIBLE
                    }
                    tmpPhoto.bitmap = bitmap
                    displayPhoto()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            takePhoto.setOnClickListener { takePhoto() }
            selectPhotoFromGallery.setOnClickListener { selectImageFromGallery() }

            tmpPhoto
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dismiss()
        show(requireParentFragment().childFragmentManager, tag)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PHOTO, tmpPhoto)
        super.onSaveInstanceState(outState)
    }

    private fun displayPhoto() {
        if (binding.addPhotoTextview.visibility == View.VISIBLE) {
            binding.addPhotoTextview.visibility =
                View.GONE
        }
        if (binding.photoImageview.visibility == View.INVISIBLE) {
            binding.photoImageview.visibility =
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

    open fun selectSingleChoicePhotoType(radioButton: RadioButton) {
        clearRadioButtons()
        radioButton.isChecked = true
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

    open fun deletePhoto() {
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
        const val TAG = "PhotoEditDialog"
        const val PHOTO = "photo"
    }
}
