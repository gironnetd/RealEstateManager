package com.openclassrooms.realestatemanager.ui.property.update

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.graphics.Color.WHITE
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentUpdateBinding
import com.openclassrooms.realestatemanager.models.*
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateViewState.UiNotification.*
import com.openclassrooms.realestatemanager.ui.property.update.view.InputFilterMinMax
import com.openclassrooms.realestatemanager.ui.property.update.view.add.AddPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.update.view.update.PhotoUpdateDialogFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.Utils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

/**
 * Fragment to edit and update a real estate.
 */
class PropertyUpdateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, var registry: ActivityResultRegistry?)
    : BaseFragment(R.layout.fragment_update), PropertyDetailFragment.OnItemClickListener,
    PhotoUpdateAdapter.OnItemClickListener, MviView<PropertyUpdateIntent, PropertyUpdateViewState> {

    private val propertyUpdateViewModel: PropertyUpdateViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentUpdateBinding? = null
    val binding get() = _binding!!

    lateinit var propertyId: String
    lateinit var property: Property
    var updatedProperty: Property = Property()

    private lateinit var updateItem: MenuItem

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    lateinit var addPhotoAlertDialog: AddPhotoDialogFragment
    lateinit var updatePhotoAlertDialog: PhotoUpdateDialogFragment

    var tmpDescription: String? = null
    var tmpPhotoType: PhotoType? = null

    private val updatePropertyIntentPublisher =
        PublishSubject.create<PropertyUpdateIntent.UpdatePropertyIntent>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)

        properties.value?.let { properties ->
            property = properties.single { property -> property.id == arguments?.getString(PROPERTY_ID) }
        }

        arguments?.let { arguments -> propertyId = arguments.getString(PROPERTY_ID).toString() }

        onBackPressedCallback()

        setHasOptionsMenu(true)
        applyDisposition()

        binding.addAPhoto.setOnClickListener {
            addPhotoAlertDialog = AddPhotoDialogFragment().also {
                it.registry = registry ?: requireActivity().activityResultRegistry
                it.tmpPhoto.propertyId = property.id
            }
            addPhotoAlertDialog.show(childFragmentManager, AddPhotoDialogFragment.TAG)
        }
        return binding.root
    }

    override fun intents(): Observable<PropertyUpdateIntent> {
        return Observable.merge(initialIntent(), loadPropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertyUpdateIntent.InitialIntent> {
        return Observable.just(PropertyUpdateIntent.InitialIntent(propertyId))
    }

    private fun loadPropertyIntentPublisher(): Observable<PropertyUpdateIntent.UpdatePropertyIntent> {
        return updatePropertyIntentPublisher
    }

    override fun render(state: PropertyUpdateViewState) {
        if(state.isSaved) {
            state.uiNotification?.let { uiNotification ->
                if(uiNotification == PROPERTIES_FULLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_totally))
                }

                if(uiNotification == PROPERTY_LOCALLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_locally))
                }
            }

            // updatedProperty.updated = false

            properties.value?.let { properties ->
                if(properties.indexOf(property) != -1) {
                    properties[properties.indexOf(property)] = updatedProperty
                    properties[properties.indexOf(property)].updated = false
                }
            }
            onBackPressed()
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    fun showDetails(propertyId: String?) {
        this.propertyId = propertyId!!

        properties.value?.let { properties ->
            property = properties.single { property -> property.id == propertyId }
        }

        updatedProperty = Property(property)

        with(binding) {
            description.setRawInputType(InputType.TYPE_CLASS_TEXT)
            val descriptionText = "\n${property.description}"
            description.setText(descriptionText)
            entryDate.text = Utils.formatDate(property.entryDate)
            entryDate.setOnClickListener { showEntryDateAlertDialog() }

            if(property.status == PropertyStatus.SOLD) {
                layoutSoldDate.visibility = View.VISIBLE
                property.soldDate?.let { soldDate.text = Utils.formatDate(it) }
            } else {
                layoutSoldDate.visibility = View.GONE
            }
            status.text = resources.getString(property.status.status)
            status.setOnClickListener { showStatusAlertDialog() }

            layoutSoldDate.setOnClickListener { showSoldDateAlertDialog() }

            interestPointsChipGroup.removeAllViewsInLayout()
            InterestPoint.values().forEach { interestPoint ->
                val newChip = layoutInflater.inflate(R.layout.layout_interest_point_chip_default,
                    binding.interestPointsChipGroup, false) as Chip
                newChip.text = resources.getString(interestPoint.place)
                newChip.isCheckable = true

                newChip.checkedIcon?.let {
                    val wrappedDrawable = DrawableCompat.wrap(it)
                    DrawableCompat.setTint(wrappedDrawable, WHITE)
                    newChip.checkedIcon = wrappedDrawable
                }

                if(property.interestPoints.contains(interestPoint)) { newChip.isChecked = true }

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

                    if(updatedProperty.interestPoints.contains(interestPointFromChip)) {
                        updatedProperty.interestPoints.remove(interestPointFromChip)
                    } else {
                        updatedProperty.interestPoints.add(interestPointFromChip!!)
                    }
                }

                interestPointsChipGroup.addView(newChip)
            }

            price.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))

            price.setText(property.price.toString())

            type.text = resources.getString(property.type.type)
            type.setOnClickListener { showTypeAlertDialog() }

            surface.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            surface.setText("${property.surface}")
            rooms.setText(property.rooms.toString())
            bathrooms.setText(property.bathRooms.toString())
            bedrooms.setText(property.bedRooms.toString())

            street.setText(property.address?.street)
            city.setText(property.address?.city)
            postalCode.setText(property.address?.postalCode)
            country.setText(property.address?.country)
            state.setText(property.address?.state)
        }

        PhotoUpdateAdapter().apply {
            binding.photosRecyclerView.adapter = this
            setOnItemClickListener(this@PropertyUpdateFragment)
            submitList(property.photos)
            notifyDataSetChanged()
        }

        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.binding.toolBar.title = property.titleInToolbar()
        }
    }

    private fun showStatusAlertDialog() {
        val items = PropertyStatus.values().filter { it != PropertyStatus.NONE }.map { resources.getString(it.status)  }
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.choose_property_status))
            var selectedItem: Int = items.indexOf(items.single { it == binding.status.text.toString() })
            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_status)) { _, _ ->
                val status = PropertyStatus.values().first { resources.getString(it.status) == items[selectedItem] }
                updatedProperty.status = status
                if(status == PropertyStatus.SOLD) {
                    binding.layoutSoldDate.visibility = View.VISIBLE
                    updatedProperty.soldDate?.let { binding.soldDate.text = Utils.formatDate(it) }
                } else {
                    binding.layoutSoldDate.visibility = View.GONE
                    binding.soldDate.text = resources.getString(R.string.no_sold_date)
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
            var selectedItem: Int = items.indexOf(items.single { it == binding.type.text.toString() })
            setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which -> selectedItem = which }
            setPositiveButton(getString(R.string.change_property_type)) { _, _ ->
                updatedProperty.type = PropertyType.values().first { resources.getString(it.type) == items[selectedItem] }
                binding.type.text = resources.getString(updatedProperty.type.type)
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            show()
        }
    }

    private fun showEntryDateAlertDialog() {
        val entryDate = Utils.fromStringToDate(binding.entryDate.text.toString())
        val calendar = Calendar.getInstance()
        calendar.time = entryDate

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
            updatedProperty.entryDate = selectedDate
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
            updatedProperty.soldDate = selectedDate
            binding.soldDate.text = Utils.formatDate(selectedDate)
        },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        updateItem = menu.findItem(R.id.navigation_update)
        updateItem.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_update -> {
                populateChanges()
                if(updatedProperty != property || updatedProperty.photos != property.photos) {
                    confirmSaveChanges()
                } else {
                    showMessage("aaaaaaaaaaa")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            clearField()
            updateItem.isVisible = false
            tmpDescription = null
            tmpPhotoType = null
            onBackPressedCallback.isEnabled = false
        } else {
            initializeToolbar()
            onBackPressedCallback.isEnabled = true
            binding.editFragment.fullScroll(ScrollView.FOCUS_UP)
        }
    }

    fun clearField() {
        with(binding) {
            description.text?.clear()
            entryDate.text = ""
            status.text = ""
            soldDate.text = resources.getString(R.string.no_sold_date)
            interestPointsChipGroup.removeAllViewsInLayout()
            price.text?.clear()
            type.text = ""
            surface.text?.clear()
            rooms.text?.clear()
            bathrooms.text?.clear()
            bedrooms.text?.clear()
            street.text?.clear()
            city.text?.clear()
            postalCode.text?.clear()
            country.text?.clear()
            state.text?.clear()
        }
    }

    private fun populateChanges() {
        with(binding) {

            updatedProperty.description = description.text.toString().substring(1)

            updatedProperty.price = price.text.toString().toInt()
            updatedProperty.surface = surface.text.toString().toInt()
            updatedProperty.rooms = rooms.text.toString().toInt()
            updatedProperty.bathRooms = bathrooms.text.toString().toInt()
            updatedProperty.bedRooms = bedrooms.text.toString().toInt()

            updatedProperty.address?.let { address ->
                address.street = street.text.toString()
                address.city = city.text.toString()
                address.postalCode = postalCode.text.toString()
                address.state = state.text.toString()
            }
        }
    }

    private fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                populateChanges()
                if(updatedProperty != property || updatedProperty.photos != property.photos) {
                    confirmSaveChanges()
                } else {
                    onBackPressed()
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    fun confirmSaveChanges() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(getString(R.string.confirm_save_changes_dialog_title))
            setMessage(getString(R.string.confirm_save_changes_dialog_message))
            setPositiveButton(getString(R.string.confirm_save_changes))  { _, _ ->
                updatedProperty.updated = true
                updatePropertyIntentPublisher.onNext(PropertyUpdateIntent.UpdatePropertyIntent(updatedProperty))
            }
            setNegativeButton(getString(R.string.no)) { _, _ -> onBackPressed() }
            show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisposition()
    }

    private fun applyDisposition() {
        this.parentFragment?.let {
            val detailFragment = this.parentFragment as NavHostFragment

            if(!resources.getBoolean(R.bool.isMasterDetail)) {
                detailFragment.requireView().layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                    (this as FrameLayout.LayoutParams).leftMargin = 0
                }
            }
        }
    }

    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val browseFragment =  this.parentFragment?.parentFragment as BrowseFragment

            with(browseFragment.binding.toolBar) {
                title = property.titleInToolbar()
                setNavigationOnClickListener {
                    populateChanges()
                    if(updatedProperty != property || updatedProperty.photos != property.photos) {
                        confirmSaveChanges()
                    } else {
                        onBackPressed()
                    }
                }
            }
        }
    }

    fun onBackPressed() {
        if (resources.getBoolean(R.bool.isMasterDetail)) {
            masterDetailOnBackPressed()
        } else {
            normalOnBackPressed()
        }
    }

    private fun masterDetailOnBackPressed() {
        (parentFragment?.parentFragment as BrowseFragment)
            .detail
            .navController
            .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                PROPERTY_ID to property.id))
    }

    private fun normalOnBackPressed() {
        (this.parentFragment?.parentFragment as BrowseFragment)
            .detail
            .navController
            .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                PROPERTY_ID to property.id))
    }

    override fun onItemClick(propertyId: String) = showDetails(propertyId = propertyId)

    override fun clickOnPhotoAtPosition(photoId: String) {
        updatePhotoAlertDialog = PhotoUpdateDialogFragment().also { it ->
            it.photo = property.photos.singleOrNull { photo -> photo.id == photoId }
            it.registry = registry ?: requireActivity().activityResultRegistry
        }
        updatePhotoAlertDialog.show(childFragmentManager, PhotoUpdateDialogFragment.TAG)
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyUpdateViewModel.states().subscribe(this::render))
        propertyUpdateViewModel.processIntents(intents())

        showDetails(propertyId)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}

