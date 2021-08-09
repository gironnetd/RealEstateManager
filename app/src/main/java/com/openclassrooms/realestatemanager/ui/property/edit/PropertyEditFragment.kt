package com.openclassrooms.realestatemanager.ui.property.edit

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.chip.Chip
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.InterestPoint
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyStatus
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import com.openclassrooms.realestatemanager.ui.property.edit.view.add.AddPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.view.update.PhotoUpdateDialogFragment
import com.openclassrooms.realestatemanager.util.Utils
import io.reactivex.disposables.CompositeDisposable
import java.util.*

abstract class PropertyEditFragment
constructor(var registry: ActivityResultRegistry?)
    : BaseFragment(R.layout.fragment_edit), PhotoUpdateAdapter.OnItemClickListener {

    private var _binding: FragmentEditBinding? = null
    val binding get() = _binding!!

    lateinit var onBackPressedCallback: OnBackPressedCallback

    lateinit var addPhotoAlertDialog: AddPhotoDialogFragment
    lateinit var updatePhotoAlertDialog: PhotoUpdateDialogFragment

    var newProperty: Property = Property()

    val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        onBackPressedCallback()

        with(binding) {
            entryDate.setOnClickListener { showEntryDateAlertDialog() }
            status.setOnClickListener { showStatusAlertDialog() }
            layoutSoldDate.setOnClickListener { showSoldDateAlertDialog() }
            type.setOnClickListener { showTypeAlertDialog() }
        }

        binding.addAPhoto.setOnClickListener {
            addPhotoAlertDialog = AddPhotoDialogFragment().also {
                it.registry = registry ?: requireActivity().activityResultRegistry
                it.tmpPhoto.propertyId = newProperty.id
            }
            addPhotoAlertDialog.show(childFragmentManager, AddPhotoDialogFragment.TAG)
        }

        return binding.root
    }

    abstract fun confirmSaveChanges()
    abstract fun onBackPressedCallback()

    abstract override fun initializeToolbar()

    fun populateChanges() {
        with(binding) {
            val none = resources.getString(R.string.none)
            newProperty.description = if(description.text.toString() != none) { description.text.toString() } else { "" }

            newProperty.price = if(price.text.toString() != none) { price.text.toString().toInt() } else { 0 }
            newProperty.surface = if(surface.text.toString() != none) { surface.text.toString().toInt() } else { 0 }
            newProperty.rooms = if(rooms.text.toString() != none) { rooms.text.toString().toInt() } else { 0 }
            newProperty.bathRooms = if(bathrooms.text.toString() != none) { bathrooms.text.toString().toInt() } else { 0 }
            newProperty.bedRooms = if(bedrooms.text.toString() != none) { bedrooms.text.toString().toInt() } else { 0 }

            newProperty.address?.let { address ->
                address.street = if(street.text.toString() != none) { street.text.toString() } else { "" }
                address.city = if(city.text.toString() != none) { city.text.toString() } else { "" }
                address.postalCode = if(postalCode.text.toString() != none) { postalCode.text.toString() } else { "" }
                address.state = if(state.text.toString() != none) { state.text.toString() } else { "" }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            clearField()
            onBackPressedCallback.isEnabled = false
        } else {
            initializeToolbar()
            onBackPressedCallback.isEnabled = true
            binding.editFragment.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    fun clearField() {
        with(binding) {
            val none = resources.getString(R.string.none)
            val colorPrimaryDark: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(R.color.colorPrimaryDark, null)
            } else {
                resources.getColor(R.color.colorPrimaryDark)
            }

            description.setText(none)
            description.setTextColor(colorPrimaryDark)

            entryDate.text = ""
            status.text = ""
            soldDate.text = ""
            price.setText(none)
            price.setTextColor(colorPrimaryDark)
            type.text = ""
            surface.setText(none)
            surface.setTextColor(colorPrimaryDark)
            rooms.setText(none)
            rooms.setTextColor(colorPrimaryDark)
            bathrooms.setText(none)
            bathrooms.setTextColor(colorPrimaryDark)
            bedrooms.setText(none)
            bedrooms.setTextColor(colorPrimaryDark)
            street.setText(none)
            street.setTextColor(colorPrimaryDark)
            city.setText(none)
            city.setTextColor(colorPrimaryDark)
            postalCode.setText(none)
            postalCode.setTextColor(colorPrimaryDark)
            country.setText(none)
            country.setTextColor(colorPrimaryDark)
            state.setText(none)
            state.setTextColor(colorPrimaryDark)
            newProperty.photos.clear()
            (photosRecyclerView.adapter as PhotoUpdateAdapter).clear()

            initInterestPoints()

            noPhotosTextView.visibility = VISIBLE
        }
    }

    fun initInterestPoints() {
        with(binding) {
            interestPointsChipGroup.removeAllViewsInLayout()
            InterestPoint.values().forEach { interestPoint ->
                val newChip = layoutInflater.inflate(R.layout.layout_interest_point_chip_default,
                    binding.interestPointsChipGroup, false) as Chip
                newChip.text = resources.getString(interestPoint.place)
                newChip.isCheckable = true

                newChip.checkedIcon?.let {
                    val wrappedDrawable = DrawableCompat.wrap(it)
                    DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
                    newChip.checkedIcon = wrappedDrawable
                }

                if(newProperty.interestPoints.contains(interestPoint)) { newChip.isChecked = true }

                newChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                newChip.setTextColor(
                    AppCompatResources.getColorStateList(
                        requireContext(),
                        R.color.chip_text_state_list
                    )
                )

                newChip.setOnClickListener {
                    val chip = it as Chip
                    val interestPointFromChip = InterestPoint.values().singleOrNull { interestPoint ->
                        resources.getString(interestPoint.place) == chip.text
                    }

                    if(newProperty.interestPoints.contains(interestPointFromChip)) {
                        newProperty.interestPoints.remove(interestPointFromChip)
                    } else {
                        newProperty.interestPoints.add(interestPointFromChip!!)
                    }
                }
                interestPointsChipGroup.addView(newChip)
            }
        }
    }

    fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showStatusAlertDialog() {
        val items = PropertyStatus.values().filter { it != PropertyStatus.NONE }.map { resources.getString(it.status)  }
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.choose_property_status))
            var selectedItem: Int = if(items.singleOrNull { it == binding.status.text.toString() } != null) {
                items.indexOf(items.singleOrNull { it == binding.status.text.toString() })
            } else { -1 }
                //items.indexOf()
            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_status)) { _, _ ->
                val status = PropertyStatus.values().first { resources.getString(it.status) == items[selectedItem] }
                newProperty.status = status
                if(status == PropertyStatus.SOLD) {
                    binding.layoutSoldDate.visibility = VISIBLE
                    newProperty.soldDate?.let { binding.soldDate.text = Utils.formatDate(it) }
                } else {
                    binding.layoutSoldDate.visibility = View.GONE
                    binding.soldDate.text = ""
                    newProperty.soldDate = null
                }
                binding.status.text = resources.getString(status.status)
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun showTypeAlertDialog() {
        val items = PropertyType.values().filter { it != PropertyType.NONE }.map { resources.getString(it.type)  }
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.choose_property_type))
            var selectedItem: Int = if(items.singleOrNull { it == binding.type.text.toString() } != null) {
                items.indexOf(items.singleOrNull { it == binding.type.text.toString() })
            } else { -1 }

            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_type)) { _, _ ->
                newProperty.type = PropertyType.values().first { resources.getString(it.type) == items[selectedItem] }
                binding.type.text = resources.getString(newProperty.type.type)
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun showEntryDateAlertDialog() {
        val entryDate = Utils.fromStringToDate(binding.entryDate.text.toString())
        val calendar = Calendar.getInstance()
        calendar.time = entryDate ?: calendar.time

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
            newProperty.entryDate = selectedDate
            binding.entryDate.text = Utils.formatDate(selectedDate)
        },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun showSoldDateAlertDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
            newProperty.soldDate = selectedDate
            binding.soldDate.text = Utils.formatDate(selectedDate)
        },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    override fun clickOnPhotoAtPosition(photoId: String) {
        updatePhotoAlertDialog = PhotoUpdateDialogFragment().also {
            it.photo = newProperty.photos.singleOrNull { photo -> photo.id == photoId }
            it.registry = registry ?: requireActivity().activityResultRegistry
        }
        updatePhotoAlertDialog.show(childFragmentManager, PhotoUpdateDialogFragment.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}