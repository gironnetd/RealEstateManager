package com.openclassrooms.realestatemanager.ui.property.search

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentSearchBinding
import com.openclassrooms.realestatemanager.models.property.InterestPoint
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.models.property.PropertyStatus
import com.openclassrooms.realestatemanager.models.property.PropertyType
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import com.openclassrooms.realestatemanager.ui.property.setting.Currency
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.Utils
import java.util.*

/**
 * Fragment to  Search one or several real estates.
 */
class PropertySearchFragment : BaseFragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    val binding get() = _binding!!

    private lateinit var innerInflater: LayoutInflater

    val mainActivity by lazy { activity as MainActivity }

    private val mainSearchFragment: MainSearchFragment by lazy { parentFragment?.parentFragment as MainSearchFragment }

    private val nearby: MutableList<InterestPoint> by lazy { mutableListOf() }

    val selectedTypes: MutableSet<PropertyType> by lazy { mutableSetOf() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val contextThemeWrapper: Context = ContextThemeWrapper(activity, R.style.AppTheme_Tertiary)
        innerInflater = inflater.cloneInContext(contextThemeWrapper)

        _binding = FragmentSearchBinding.inflate(innerInflater, container, false)

        super.onCreateView(innerInflater, container, savedInstanceState)
        initMinMaxPricesWithDefaultCurrency()
        return binding.root
    }

    private fun initMinMaxPricesWithDefaultCurrency() {
        defaultCurrency.observe(viewLifecycleOwner) { defaultCurrency ->
            with(binding) {
                when (defaultCurrency) {
                    Currency.EUROS.currency -> {
                        minPriceTextInputLayout.endIconDrawable = ResourcesCompat.getDrawable(
                            resources, R.drawable.ic_baseline_euro_24,
                            null
                        )
                        maxPriceTextInputLayout.endIconDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_euro_24,
                            null
                        )
                    }
                    Currency.DOLLARS.currency -> {
                        minPriceTextInputLayout.endIconDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_dollar_24,
                            null
                        )
                        maxPriceTextInputLayout.endIconDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_dollar_24,
                            null
                        )
                    }
                }
            }
        }
    }

    fun initResultMenuItem() {
        (mainActivity.binding.toolBar.menu.findItem(R.id.navigation_result_search).actionView as LinearLayout)
            .setOnClickListener {
                findProperties()
            }
        /*if (!mainActivity.onBackPressedCallback.isEnabled) {
            mainActivity.onBackPressedCallback.isEnabled = true
        }*/
    }

    private fun findProperties() {
        var filteredProperties: List<Property> = properties.value!!.map { property -> property.deepCopy() }

        if (selectedTypes.isNotEmpty()) {
            filteredProperties = filteredProperties.filter { property -> selectedTypes.contains(property.type) }
        }

        with(binding) {
            if (inSaleRadioButton.isChecked) {
                filteredProperties = filteredProperties.filter { property -> property.status == PropertyStatus.IN_SALE }

                if (inSaleTextEdit.text.toString() != none) {
                    filteredProperties = filteredProperties.filter {
                        property ->
                        property.entryDate!!.time >=
                            Utils.fromStringToDate(inSaleTextEdit.text.toString()).time
                    }
                }
            }

            if (forRentRadioButton.isChecked) {
                filteredProperties = filteredProperties.filter { property ->
                    property.status == PropertyStatus.FOR_RENT
                }

                if (forRentTextEdit.text.toString() != none) {
                    filteredProperties = filteredProperties.filter {
                        property ->
                        property.entryDate!!.time >=
                            Utils.fromStringToDate(forRentTextEdit.text.toString()).time
                    }
                }
            }

            if (soldRadioButton.isChecked) {
                filteredProperties = filteredProperties.filter { property -> property.status == PropertyStatus.SOLD }

                if (soldEntryDateTextEdit.text.toString() != none) {
                    filteredProperties = filteredProperties.filter {
                        property ->
                        property.entryDate!!.time >=
                            Utils.fromStringToDate(soldEntryDateTextEdit.text.toString()).time
                    }
                }

                if (soldDateTextEdit.text.toString() != none) {
                    filteredProperties = filteredProperties.filter {
                        property ->
                        property.soldDate!!.time <=
                            Utils.fromStringToDate(soldDateTextEdit.text.toString()).time
                    }
                }
            }

            if (minPrice.text.toString() != none && minPrice.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.price >= minPrice.text.toString().toInt()
                }
            }

            if (maxPrice.text.toString() != none && maxPrice.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.price <= maxPrice.text.toString().toInt()
                }
            }

            if (minSurface.text.toString() != none && minSurface.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.surface >= minSurface.text.toString().toInt()
                }
            }

            if (maxSurface.text.toString() != none && maxSurface.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.surface <= maxSurface.text.toString().toInt()
                }
            }

            if (minRooms.text.toString() != none && minRooms.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.rooms >= minRooms.text.toString().toInt()
                }
            }

            if (maxRooms.text.toString() != none && maxRooms.text.toString().isNotEmpty()) {
                filteredProperties = filteredProperties.filter {
                    property ->
                    property.rooms <= maxRooms.text.toString().toInt()
                }
            }
        }

        if (nearby.isNotEmpty()) {
            filteredProperties = filteredProperties.filter {
                property ->
                property.interestPoints.any { interestPoint -> nearby.contains(interestPoint) }
            }
        }

        if (filteredProperties.isNotEmpty()) {
            BrowseResultFragment.searchedProperties.value = filteredProperties.toMutableList()
            (parentFragment as NavHostFragment).navController.navigate(R.id.navigation_browse_search_result)
            mainActivity.binding.toolBar.visibility = View.GONE
            mainSearchFragment.binding.toolBar.visibility = View.VISIBLE
        } else {
            mainActivity.showMessage(innerInflater.context, resources.getString(R.string.no_property_found))
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            initResultMenuItem()
        }
    }

    override fun initializeToolbar() {
        initInterestPoints()
        configureView()
    }

    private fun initInterestPoints() {
        with(binding) {
            interestPointsChipGroup.removeAllViewsInLayout()
            InterestPoint.values().forEach { interestPoint ->
                if (interestPoint != InterestPoint.NONE) {
                    val newChip = innerInflater.inflate(
                        R.layout.layout_interest_point_chip_default,
                        binding.interestPointsChipGroup, false
                    ) as Chip
                    newChip.text = resources.getString(interestPoint.place)
                    newChip.isCheckable = true

                    newChip.checkedIcon?.let {
                        val wrappedDrawable = DrawableCompat.wrap(it)
                        DrawableCompat.setTint(wrappedDrawable, Color.WHITE)
                        newChip.checkedIcon = wrappedDrawable
                    }

                    newChip.isChecked = false

                    newChip.setOnClickListener {
                        val chip = it as Chip
                        val interestPointFromChip = InterestPoint.values().singleOrNull { interestPoint ->
                            resources.getString(interestPoint.place) == chip.text
                        }

                        if (nearby.contains(interestPointFromChip)) {
                            nearby.remove(interestPointFromChip)
                        } else {
                            nearby.add(interestPointFromChip!!)
                        }
                    }
                    interestPointsChipGroup.addView(newChip)
                }
            }
        }
    }

    private fun configureView() {
        with(binding) {
            allType.setOnClickListener {
                if ((it as CheckBox).isChecked) {
                    flatCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.FLAT)
                    townhouseCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.TOWNHOUSE)
                    penthouseCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.PENTHOUSE)
                    houseCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.HOUSE)
                    duplexCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.DUPLEX)
                    noneCheckbox.isChecked = true
                    selectedTypes.add(PropertyType.NONE)
                } else {
                    flatCheckbox.isChecked = false
                    townhouseCheckbox.isChecked = false
                    penthouseCheckbox.isChecked = false
                    houseCheckbox.isChecked = false
                    duplexCheckbox.isChecked = false
                    noneCheckbox.isChecked = false
                    selectedTypes.clear()
                }
            }

            flatCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }
            townhouseCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }
            penthouseCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }
            houseCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }
            duplexCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }
            noneCheckbox.setOnClickListener { onTypeCheckBoxClick(it) }

            inSaleRadioButton.setOnClickListener { onStatusRadioButtonClick(it) }
            forRentRadioButton.setOnClickListener { onStatusRadioButtonClick(it) }
            soldRadioButton.setOnClickListener { onStatusRadioButtonClick(it) }

            inSaleTextEdit.setOnClickListener { showDateAlertDialog(it as TextInputEditText) }
            forRentTextEdit.setOnClickListener { showDateAlertDialog(it as TextInputEditText) }
            soldEntryDateTextEdit.setOnClickListener { showDateAlertDialog(it as TextInputEditText) }
            soldDateTextEdit.setOnClickListener { showDateAlertDialog(it as TextInputEditText) }

            val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if (hasFocus && this == none) {
                        view.setText("")
                        view.setTextColor(Color.BLACK)
                    }
                    if (!hasFocus && (this == "" || this == "0")) {
                        view.setText(none)
                        view.setTextColor(colorPrimary)
                    }
                }
            }

            minPrice.onFocusChangeListener = onFocusChangeListener
            maxPrice.onFocusChangeListener = onFocusChangeListener
            minSurface.onFocusChangeListener = onFocusChangeListener
            maxSurface.onFocusChangeListener = onFocusChangeListener
            minRooms.onFocusChangeListener = onFocusChangeListener
            maxRooms.onFocusChangeListener = onFocusChangeListener
        }
    }

    private fun onStatusRadioButtonClick(view: View) {
        with(binding) {
            when (view.id) {
                R.id.in_sale_radio_button -> {
                    if (inSaleTextInputLayout.isEnabled) {
                        inSaleTextInputLayout.isEnabled = false
                        inSaleRadioButton.isChecked = false
                    } else {
                        inSaleRadioButton.isChecked = true
                        inSaleTextInputLayout.isEnabled = true

                        forRentRadioButton.isChecked = false
                        forRentTextInputLayout.isEnabled = false

                        soldRadioButton.isChecked = false
                        soldEntryDateTextInputLayout.isEnabled = false
                        soldDateTextInputLayout.isEnabled = false
                    }
                }
                R.id.for_rent_radio_button -> {
                    if (forRentTextInputLayout.isEnabled) {
                        forRentTextInputLayout.isEnabled = false
                        forRentRadioButton.isChecked = false
                    } else {
                        inSaleRadioButton.isChecked = false
                        inSaleTextInputLayout.isEnabled = false

                        forRentTextInputLayout.isEnabled = true
                        forRentRadioButton.isChecked = true

                        soldRadioButton.isChecked = false
                        soldEntryDateTextInputLayout.isEnabled = false
                        soldDateTextInputLayout.isEnabled = false
                    }
                }
                R.id.sold_radio_button -> {
                    if (soldEntryDateTextInputLayout.isEnabled && soldDateTextInputLayout.isEnabled) {
                        soldEntryDateTextInputLayout.isEnabled = false
                        soldDateTextInputLayout.isEnabled = false
                        soldRadioButton.isChecked = false
                    } else {
                        inSaleRadioButton.isChecked = false
                        inSaleTextInputLayout.isEnabled = false

                        forRentRadioButton.isChecked = false
                        forRentTextInputLayout.isEnabled = false

                        soldRadioButton.isChecked = true
                        soldEntryDateTextInputLayout.isEnabled = true
                        soldDateTextInputLayout.isEnabled = true
                    }
                }
            }
        }
    }

    private fun onTypeCheckBoxClick(view: View) {
        with(binding) {
            when (view.id) {
                flatCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.FLAT)) {
                        flatCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.FLAT)
                    } else {
                        flatCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.FLAT)
                    }
                }
                townhouseCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.TOWNHOUSE)) {
                        townhouseCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.TOWNHOUSE)
                    } else {
                        townhouseCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.TOWNHOUSE)
                    }
                }
                penthouseCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.PENTHOUSE)) {
                        penthouseCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.PENTHOUSE)
                    } else {
                        penthouseCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.PENTHOUSE)
                    }
                }
                houseCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.HOUSE)) {
                        houseCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.HOUSE)
                    } else {
                        houseCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.HOUSE)
                    }
                }
                duplexCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.DUPLEX)) {
                        duplexCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.DUPLEX)
                    } else {
                        duplexCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.DUPLEX)
                    }
                }
                noneCheckbox.id -> {
                    if (!selectedTypes.contains(PropertyType.NONE)) {
                        noneCheckbox.isChecked = true
                        selectedTypes.add(PropertyType.NONE)
                    } else {
                        noneCheckbox.isChecked = false
                        selectedTypes.remove(PropertyType.NONE)
                    }
                }
                else -> {}
            }
        }
    }

    private fun showDateAlertDialog(editText: TextInputEditText) {
        val actualDate = if (editText.text!!.isNotEmpty()) {
            Utils.fromStringToDate(editText.text.toString())
        } else {
            null
        }
        val calendar = Calendar.getInstance()
        calendar.time = actualDate ?: calendar.time

        DatePickerDialog(
            innerInflater.context,
            { _, year, month, dayOfMonth ->
                val selectedDate = GregorianCalendar(year, month, dayOfMonth, 0, 0).time
                editText.setText(Utils.formatDate(selectedDate))
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
}
