package com.openclassrooms.realestatemanager.ui.property.edit

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.InterestPoint
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyStatus
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
import com.openclassrooms.realestatemanager.ui.property.edit.view.add.AddPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.edit.view.update.PhotoUpdateDialogFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseBrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.Utils
import io.reactivex.disposables.CompositeDisposable
import java.util.*


abstract class PropertyEditFragment
constructor(var registry: ActivityResultRegistry?)
    : BaseFragment(R.layout.fragment_edit), PhotoUpdateAdapter.OnItemClickListener {

    var _binding: FragmentEditBinding? = null
    val binding get() = _binding!!

    lateinit var onBackPressedCallback: OnBackPressedCallback

    lateinit var addPhotoAlertDialog: AddPhotoDialogFragment
    lateinit var updatePhotoAlertDialog: PhotoUpdateDialogFragment

    var newProperty: Property = Property()

    val compositeDisposable = CompositeDisposable()

    val baseBrowseFragment by lazy { requireParentFragment().parentFragment as BaseBrowseFragment }

    abstract fun confirmSaveChanges()
    abstract fun onBackPressedCallback()
    abstract fun layoutInflater(): LayoutInflater

    abstract override fun initializeToolbar()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        setHasOptionsMenu(true)
        onBackPressedCallback()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    fun populateChanges() {
        with(binding) {
            newProperty.description = if(description.text.toString() != none) { description.text.toString() } else { "" }

            newProperty.price = if(price.text.toString() != none) { price.text.toString().toInt() } else { 0 }
            newProperty.surface = if(surface.text.toString() != none) { surface.text.toString().toInt() } else { 0 }
            newProperty.rooms = if(rooms.text.toString() != none) { rooms.text.toString().toInt() } else { 0 }
            newProperty.bathRooms = if(bathrooms.text.toString() != none) { bathrooms.text.toString().toInt() } else { 0 }
            newProperty.bedRooms = if(bedrooms.text.toString() != none) { bedrooms.text.toString().toInt() } else { 0 }

            newProperty.address.let { address ->
                address.street = if(street.text.toString() != none) { street.text.toString() } else { "" }
                address.city = if(city.text.toString() != none) { city.text.toString() } else { "" }
                address.postalCode = if(postalCode.text.toString() != none) { postalCode.text.toString() } else { "" }
                address.state = if(state.text.toString() != none) { state.text.toString() } else { "" }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) { binding.editFragment.fullScroll(ScrollView.FOCUS_UP) }
    }

    fun clearField() {
        with(binding) {
            newProperty.photos.clear()
            photosRecyclerView.adapter?.let {
                (photosRecyclerView.adapter as PhotoUpdateAdapter).clear()
            }
            initInterestPoints()
            noPhotos.visibility = VISIBLE
        }
    }

    open fun configureView() {
        with(binding) {
            binding.addAPhoto.setOnClickListener {
                addPhotoAlertDialog = AddPhotoDialogFragment().also {
                    it.registry = registry ?: requireActivity().activityResultRegistry
                    it.tmpPhoto.propertyId = newProperty.id
                }
                addPhotoAlertDialog.show(childFragmentManager, AddPhotoDialogFragment.TAG)
            }

            entryDate.setOnClickListener { showEntryDateAlertDialog() }
            status.setOnClickListener { showStatusAlertDialog() }
            soldDate.setOnClickListener { showSoldDateAlertDialog() }
            type.setOnClickListener { showTypeAlertDialog() }

            val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        view.setText("")
                        view.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && (this == "" || this == "0")) {
                        view.setText(none)
                        view.setTextColor(colorPrimary)
                    }
                }
            }

            with(description) {
                setRawInputType(TYPE_CLASS_TEXT)
                setText(run {
                    if(newProperty.description.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.description
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(entryDate) {
                newProperty.entryDate?.let {
                    setText(Utils.formatDate(newProperty.entryDate))
                    setTextColor(Color.BLACK)
                }
            }

            with(status) {
                if(newProperty.status != PropertyStatus.NONE) {
                    setText(resources.getString(newProperty.status.status))
                    setTextColor(Color.BLACK)
                }
            }

            with(soldDateTextInputLayout) {
                if(newProperty.status == PropertyStatus.SOLD) {
                    visibility =VISIBLE
                    newProperty.soldDate?.let {
                        soldDate.setText(Utils.formatDate(it))
                    }
                } else {
                    visibility = GONE
                }
            }

            initInterestPoints()

            //price.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            with(price) {
                setText(run {
                    if(newProperty.price != 0) {
                        setTextColor(Color.BLACK)
                        newProperty.price.toString()
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(type) {
                if(newProperty.type != PropertyType.NONE) {
                    setText(resources.getString(newProperty.type.type))
                    setTextColor(Color.BLACK)
                }
            }

            //surface.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            with(surface) {
                setText(run {
                    if(newProperty.surface != 0) {
                        setTextColor(Color.BLACK)
                        newProperty.surface.toString()
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(rooms) {
                setText(run {
                    if(newProperty.rooms != 0) {
                        setTextColor(Color.BLACK)
                        newProperty.rooms.toString()
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(bedrooms) {
                setText(run {
                    if(newProperty.bedRooms != 0) {
                        setTextColor(Color.BLACK)
                        newProperty.bedRooms.toString()
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(bathrooms) {
                setText(run {
                    if(newProperty.bathRooms != 0) {
                        setTextColor(Color.BLACK)
                        newProperty.bathRooms.toString()
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            bathrooms.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        bathrooms.setText("")
                        bathrooms.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        bathrooms.setText(none)
                        bathrooms.setTextColor(colorPrimary)
                    }
                }
            }

            with(street) {
                setText(run {
                    if(newProperty.address.street.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.address.street
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(city) {
                setText( run {
                    if(newProperty.address.city.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.address.city
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(postalCode) {
                setText( run {
                    if(newProperty.address.postalCode.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.address.postalCode
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(country) {
                setText( run {
                    if(newProperty.address.country.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.address.country
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            with(state) {
                setText( run {
                    if(newProperty.address.state.isNotEmpty()) {
                        setTextColor(Color.BLACK)
                        newProperty.address.state
                    } else { none }
                })
                setOnFocusChangeListener(onFocusChangeListener)
            }

            PhotoUpdateAdapter().apply {
                if(newProperty.photos.isNotEmpty()) { noPhotos.visibility = GONE }
                binding.photosRecyclerView.adapter = this
                setOnItemClickListener(this@PropertyEditFragment)
                submitList(newProperty.photos)
            }
        }
    }

    fun clearView() {
        newProperty = Property()
        clearField()
    }

    open fun initInterestPoints() {
        with(binding) {
            interestPointsChipGroup.removeAllViewsInLayout()
            InterestPoint.values().forEach { interestPoint ->
                if(interestPoint != InterestPoint.NONE) {
                    val newChip = layoutInflater().inflate(R.layout.layout_interest_point_chip_default,
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
    }

    fun showMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showStatusAlertDialog() {
        val items = PropertyStatus.values().filter { it != PropertyStatus.NONE }.map { resources.getString(it.status)  }
        val builder = AlertDialog.Builder(layoutInflater().context)
        with(builder) {
            setTitle(resources.getString(R.string.choose_property_status))
            var selectedItem: Int = if(items.singleOrNull { it == binding.status.text.toString() } != null) {
                items.indexOf(items.singleOrNull { it == binding.status.text.toString() })
            } else { -1 }
            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_status)) { _, _ ->
                val status = PropertyStatus.values().first { resources.getString(it.status) == items[selectedItem] }
                newProperty.status = status
                if(status == PropertyStatus.SOLD) {
                    binding.soldDateTextInputLayout.visibility = VISIBLE
                    newProperty.soldDate?.let { binding.soldDate.setText(Utils.formatDate(it)) }
                } else {
                    binding.soldDateTextInputLayout.visibility = GONE
                    binding.soldDate.setText("")
                    newProperty.soldDate = null
                }
                binding.status.setText(resources.getString(status.status))
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun showTypeAlertDialog() {
        val items = PropertyType.values().filter { it != PropertyType.NONE }.map { resources.getString(it.type)  }
        val builder = AlertDialog.Builder(layoutInflater().context)
        with(builder) {
            setTitle(resources.getString(R.string.choose_property_type))
            var selectedItem: Int = if(items.singleOrNull { it == binding.type.text.toString() } != null) {
                items.indexOf(items.singleOrNull { it == binding.type.text.toString() })
            } else { -1 }

            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_type)) { _, _ ->
                newProperty.type = PropertyType.values().first { resources.getString(it.type) == items[selectedItem] }
                binding.type.setText(resources.getString(newProperty.type.type))
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun showEntryDateAlertDialog() {
        val entryDate = if(binding.entryDate.text!!.isNotEmpty()) { Utils.fromStringToDate(binding.entryDate.text.toString()) } else { null }
        val calendar = Calendar.getInstance()
        calendar.time = entryDate ?: calendar.time

        DatePickerDialog(layoutInflater().context, { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
            newProperty.entryDate = selectedDate
            binding.entryDate.setText(Utils.formatDate(selectedDate))
        },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun showSoldDateAlertDialog() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        DatePickerDialog(layoutInflater().context, { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
            newProperty.soldDate = selectedDate
            binding.soldDate.setText(Utils.formatDate(selectedDate))
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

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }
}