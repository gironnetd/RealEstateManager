package com.openclassrooms.realestatemanager.ui.property.edit.view.add

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDialogAddPhotoBinding
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseDialogFragment
import com.openclassrooms.realestatemanager.util.Constants
import java.io.File
import java.io.FileOutputStream

class AddPhotoDialogFragment : BaseDialogFragment(R.layout.fragment_dialog_add_photo) {

    private var _binding: FragmentDialogAddPhotoBinding? = null
    val binding get() = _binding!!

    var tmpPhoto: Photo = Photo()
    var latestTmpUri: Uri? = null
    var tmpFile: File? = null

    var registry: ActivityResultRegistry? = null
    private lateinit var selectImageFromGalleryResult: ActivityResultLauncher<String>
    private lateinit var takeImageResult: ActivityResultLauncher<Void?>

    private lateinit var parentEditFragment: PropertyEditFragment

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogAddPhotoBinding.inflate(LayoutInflater.from(context))

        return activity?.let {
            MaterialAlertDialogBuilder(requireContext()).run {

                setView(binding.root)
                setPositiveButton(getString(R.string.add_photo)) { _, _ ->

                    parentFragment?.let { parentFragment ->
                        when(parentFragment::class.java) {
                            PropertyUpdateFragment::class.java -> { parentEditFragment = parentFragment as PropertyUpdateFragment }
                            PropertyCreateFragment::class.java -> { parentEditFragment = parentFragment as PropertyCreateFragment }
                        }
                    }

                    if(binding.descriptionTextInputLayout.editText?.text.toString() != resources.getString(R.string.enter_a_description)) {
                        tmpPhoto.description = binding.descriptionTextInputLayout.editText?.text.toString()
                    }

                    if(!tmpPhoto.mainPhoto && binding.isMainPhoto.isChecked) {
                        parentEditFragment.newProperty.photos.singleOrNull { it.mainPhoto }?.let { photo ->
                            photo.mainPhoto = false
                        }
                        tmpPhoto.mainPhoto = true
                        parentEditFragment.newProperty.mainPhotoId = tmpPhoto.id
                    }

                    if(binding.photoImageview.drawable != null) {
                        if(parentEditFragment is PropertyUpdateFragment ) {
                            val bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap

                            tmpPhoto.id = Firebase.firestore.collection(Constants.PROPERTIES_COLLECTION)
                                .document(tmpPhoto.propertyId)
                                .collection(Constants.PHOTOS_COLLECTION)
                                .document().id

                            val outputStream = FileOutputStream(
                                File(tmpPhoto.storageLocalDatabase(requireContext().cacheDir, true)), true)

                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        }
                        tmpPhoto.bitmap = (binding.photoImageview.drawable as BitmapDrawable).bitmap
                    }

                    tmpFile?.delete()

                    tmpPhoto.locallyCreated = true
                    parentEditFragment.newProperty.photos.add(parentEditFragment.newProperty.photos.size, tmpPhoto)

                    if(parentEditFragment.binding.noPhotos.visibility == VISIBLE) {
                        parentEditFragment.binding.noPhotos.visibility = GONE
                    }

                    with(parentEditFragment.binding.photosRecyclerView.adapter as PhotoUpdateAdapter) {
                        submitList(parentEditFragment.newProperty.photos)
                    }
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

            descriptionTextInputLayout.editText?.imeOptions = EditorInfo.IME_ACTION_DONE
            descriptionTextInputLayout.editText?.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)

            descriptionTextInputLayout.editText?.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus && descriptionTextInputLayout.editText?.text.toString()
                    == resources.getString(R.string.enter_a_description)) {
                    descriptionTextInputLayout.editText?.text?.clear()
                } else if(!hasFocus && descriptionTextInputLayout.editText?.text.toString().isEmpty()) {
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

            selectImageFromGalleryResult =  registerForActivityResult(ActivityResultContracts.GetContent(), registry ?: requireActivity().activityResultRegistry) { uri: Uri? ->
                uri?.let {
                    latestTmpUri = uri
                    tmpFile?.delete()
                    tmpFile = File(requireContext().cacheDir, latestTmpUri!!.lastPathSegment!!)
                    if(binding.deletePhoto.visibility == View.GONE) { binding.deletePhoto.visibility =
                        VISIBLE
                    }
                    displayPhoto()
                }
            }

            takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicturePreview(), registry ?: requireActivity().activityResultRegistry) { bitmap ->
                try {
                    val outputStream = FileOutputStream(
                        File(requireContext().cacheDir, latestTmpUri?.lastPathSegment!!), true)

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                    if(binding.deletePhoto.visibility == View.GONE) { binding.deletePhoto.visibility =
                        VISIBLE
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

    private fun displayPhoto() {
        if(binding.addPhotoTextview.visibility == VISIBLE) { binding.addPhotoTextview.visibility =
            GONE
        }
        if(binding.photoImageview.visibility == View.INVISIBLE) { binding.photoImageview.visibility =
            VISIBLE
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
            R.id.radio_button_lounge -> { tmpPhoto.type = PhotoType.LOUNGE }
            R.id.radio_button_facade -> { tmpPhoto.type = PhotoType.FACADE }
            R.id.radio_button_kitchen -> { tmpPhoto.type = PhotoType.KITCHEN }
            R.id.radio_button_bedroom -> { tmpPhoto.type = PhotoType.BEDROOM }
            R.id.radio_button_bathroom -> { tmpPhoto.type = PhotoType.BATHROOM }
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
            addPhotoTextview.visibility = VISIBLE
        }
    }

    companion object {
        const val TAG = "PhotoEditDialog"
    }
}