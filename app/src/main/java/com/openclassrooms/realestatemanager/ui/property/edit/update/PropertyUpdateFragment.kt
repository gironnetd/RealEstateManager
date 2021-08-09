package com.openclassrooms.realestatemanager.ui.property.edit.update

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.*
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.*
import com.openclassrooms.realestatemanager.ui.property.edit.util.InputFilterMinMax
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

/**
 * Fragment to edit and update a real estate.
 */
class PropertyUpdateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?)
    : PropertyEditFragment(registry), PropertyDetailFragment.OnItemClickListener,
    MviView<PropertyEditIntent.PropertyUpdateIntent, PropertyEditViewState> {

    private val propertyUpdateViewModel: PropertyUpdateViewModel by viewModels { viewModelFactory }

    lateinit var propertyId: String
    lateinit var property: Property

    private lateinit var updateItem: MenuItem

    private val updatePropertyIntentPublisher =
        PublishSubject.create<UpdatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle? ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        properties.value?.let { properties ->
            property = properties.single { property -> property.id == arguments?.getString(PROPERTY_ID) }
        }

        arguments?.let { arguments -> propertyId = arguments.getString(PROPERTY_ID).toString() }

        onBackPressedCallback()
        applyDisposition()

        return binding.root
    }

    override fun intents(): Observable<PropertyEditIntent.PropertyUpdateIntent> {
        return Observable.merge(initialIntent(), loadPropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertyEditIntent.PropertyUpdateIntent.InitialIntent> {
        return Observable.just(PropertyEditIntent.PropertyUpdateIntent.InitialIntent(propertyId))
    }

    private fun loadPropertyIntentPublisher(): Observable<UpdatePropertyIntent> {
        return updatePropertyIntentPublisher
    }

    override fun render(state: PropertyEditViewState) {
        if(state.isSaved) {
            state.uiNotification?.let { uiNotification ->
                if(uiNotification == PROPERTIES_FULLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_totally))
                }

                if(uiNotification == PROPERTY_LOCALLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_locally))
                }
            }

            properties.value?.let { properties ->
                if(properties.indexOf(property) != -1) {
                    properties[properties.indexOf(property)] = newProperty
                    properties[properties.indexOf(newProperty)].updated = false
                }
            }
            onBackPressed()
        }
    }

    fun showDetails(propertyId: String?) {
        this.propertyId = propertyId!!

        properties.value?.let { properties ->
            property = properties.single { property -> property.id == propertyId }
        }

        newProperty = Property(property)

        with(binding) {
            description.setRawInputType(InputType.TYPE_CLASS_TEXT)
            description.setText(property.description)
            description.setTextColor(Color.BLACK)
            entryDate.text = Utils.formatDate(property.entryDate)
            entryDate.setTextColor(Color.BLACK)
            if(property.status == PropertyStatus.SOLD) {
                layoutSoldDate.visibility = View.VISIBLE
                property.soldDate?.let { soldDate.text = Utils.formatDate(it) }
            } else {
                layoutSoldDate.visibility = View.GONE
            }
            status.text = resources.getString(property.status.status)
            status.setTextColor(Color.BLACK)

            initInterestPoints()

            price.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))

            price.setText(property.price.toString())
            price.setTextColor(Color.BLACK)

            type.text = resources.getString(property.type.type)
            type.setTextColor(Color.BLACK)

            surface.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            surface.setText("${property.surface}")
            surface.setTextColor(Color.BLACK)

            rooms.setText(property.rooms.toString())
            rooms.setTextColor(Color.BLACK)

            bathrooms.setText(property.bathRooms.toString())
            bathrooms.setTextColor(Color.BLACK)

            bedrooms.setText(property.bedRooms.toString())
            bedrooms.setTextColor(Color.BLACK)

            street.setText(property.address?.street)
            street.setTextColor(Color.BLACK)

            city.setText(property.address?.city)
            city.setTextColor(Color.BLACK)

            postalCode.setText(property.address?.postalCode)
            postalCode.setTextColor(Color.BLACK)

            country.setText(property.address?.country)
            country.setTextColor(Color.BLACK)

            state.setText(property.address?.state)
            state.setTextColor(Color.BLACK)

            if(property.photos.isNotEmpty()) {
                noPhotosTextView.visibility = View.GONE
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        updateItem = menu.findItem(R.id.navigation_update)
        updateItem.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_update -> {
                populateChanges()
                if(newProperty != property || newProperty.photos != property.photos) {
                    confirmSaveChanges()
                } else {
                    showMessage(resources.getString(R.string.no_changes))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            clearField()
            updateItem.isVisible = false
        } else {
            // initializeToolbar()
            updateItem.isVisible = true
        }
    }

    override fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                populateChanges()
                if(newProperty != property || newProperty.photos != property.photos) {
                    confirmSaveChanges()
                } else {
                    onBackPressed()
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun confirmSaveChanges() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(getString(R.string.confirm_save_changes_dialog_title))
            setMessage(getString(R.string.confirm_save_changes_dialog_message))
            setPositiveButton(getString(R.string.confirm_save_changes))  { _, _ ->
                newProperty.updated = true
                updatePropertyIntentPublisher.onNext(UpdatePropertyIntent(
                    newProperty))
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
                    if(newProperty != property || newProperty.photos != property.photos) {
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

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyUpdateViewModel.states().subscribe(this::render))
        propertyUpdateViewModel.processIntents(intents())

        showDetails(propertyId)
    }
}

