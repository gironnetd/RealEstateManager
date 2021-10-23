package com.openclassrooms.realestatemanager.ui.property.edit.update

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent.InitialIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment.Companion.TAG
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.update.UpdateLocationDialogFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to edit and update a real estate.
 */
class PropertyUpdateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?) :
    PropertyEditFragment(registry),
    MviView<PropertyUpdateIntent, PropertyEditViewState>,
    UpdateLocationDialogFragment.UpdateLocationListener {

    private val propertyUpdateViewModel: PropertyUpdateViewModel by viewModels { viewModelFactory }

    lateinit var property: Property
    private lateinit var updateItem: MenuItem
    private lateinit var searchItem: MenuItem

    private lateinit var innerInflater: LayoutInflater

    lateinit var updateLocationAlertDialog: UpdateLocationDialogFragment

    private val updatePropertyIntentPublisher = PublishSubject.create<UpdatePropertyIntent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        properties.value?.let { properties ->
            property = properties.single { property -> property.id == arguments?.getString(PROPERTY_ID) }
            newProperty = property.deepCopy()
        }
        applyDisposition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        when (baseBrowseFragment::class.java) {
            BrowseResultFragment::class.java -> {
                innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Tertiary))
            }
            BrowseFragment::class.java -> {
                innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Primary))
            }
        }
        editBinding = FragmentEditBinding.inflate(innerInflater, container, false)
        initializeToolbar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compositeDisposable.add(propertyUpdateViewModel.states().subscribe(this::render))
        propertyUpdateViewModel.processIntents(intents())
        showDetails(property.id)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_update).isVisible = true
        baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search).isVisible = false
        super.onPrepareOptionsMenu(baseBrowseFragment.binding.toolBar.menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!::updateItem.isInitialized) {
            updateItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_update)
        }
        updateItem.isVisible = true

        // getting Linear Layout from custom layout
        val updateItemLayout = updateItem.actionView as LinearLayout

        updateItemLayout.apply {
            findViewById<ImageView>(R.id.menu_item_icon).setImageResource(R.drawable.ic_baseline_update_24)
            findViewById<TextView>(R.id.menu_item_title).text = resources.getString(R.string.update)
        }

        updateItemLayout.setOnClickListener {
            populateChanges()
            if (newProperty != property || newProperty.photos != property.photos) {
                confirmSaveChanges()
            } else {
                showMessage(resources.getString(R.string.no_changes))
            }
        }

        if (!::searchItem.isInitialized) {
            searchItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search)
        }
        searchItem.isVisible = false
        super.onCreateOptionsMenu(baseBrowseFragment.binding.toolBar.menu, inflater)
    }

    override fun intents(): Observable<PropertyUpdateIntent> {
        return Observable.merge(initialIntent(), loadPropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<InitialIntent> {
        return Observable.just(InitialIntent(property.id))
    }

    private fun loadPropertyIntentPublisher(): Observable<UpdatePropertyIntent> {
        return updatePropertyIntentPublisher
    }

    override fun render(state: PropertyEditViewState) {
        if (state.isSaved) {
            state.uiNotification?.let { uiNotification ->
                if (uiNotification == PROPERTIES_FULLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_totally))
                }

                if (uiNotification == PROPERTY_LOCALLY_UPDATED) {
                    showMessage(resources.getString(R.string.property_update_locally))
                }
            }

            properties.value?.let { properties ->
                properties[properties.indexOf(property)] = newProperty
                BaseFragment.properties.value = properties
            }
            onBackPressed()
        }
    }

    fun showDetails(propertyId: String) {
        clearField()
        properties.value?.let { properties ->
            property = properties.single { property -> property.id == propertyId }
            newProperty = property.deepCopy()
        }
        configureView()

        baseBrowseFragment.binding.toolBar.title = property.titleInToolbar(resources)
    }

    override fun configureView() {
        super.configureView()
        with(binding) {
            PhotoUpdateAdapter(layoutInflater().context).apply {
                if (newProperty.photos.isNotEmpty()) {
                    noPhotos.visibility = View.GONE
                }
                photosRecyclerView.adapter = this
                setOnItemClickListener(this@PropertyUpdateFragment)
                submitList(newProperty.photos)
            }
            mapViewButton.setImageResource(R.drawable.ic_baseline_edit_location_36)

            mapViewButton.setOnClickListener {
                if (!::updateLocationAlertDialog.isInitialized) {
                    updateLocationAlertDialog = UpdateLocationDialogFragment(
                        innerContext = innerInflater.context, newProperty.address
                    )
                    updateLocationAlertDialog.show(childFragmentManager, TAG)
                    updateLocationAlertDialog.setCallBack(this@PropertyUpdateFragment)
                } else {
                    updateLocationAlertDialog.address = newProperty.address
                    updateLocationAlertDialog.alertDialog.show()
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            baseBrowseFragment.binding.toolBar.setNavigationOnClickListener(null)
            clearView()
        } else {
            initializeToolbar()
            updateItem.isVisible = true
            onBackPressedCallback.isEnabled = true
        }
    }

    override fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isHidden) {
                    populateChanges()
                    if (newProperty != property || newProperty.photos != property.photos) {
                        confirmSaveChanges()
                    } else {
                        onBackPressed()
                    }
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun layoutInflater(): LayoutInflater {
        return innerInflater
    }

    fun confirmSaveChanges() {
        val builder = AlertDialog.Builder(innerInflater.context)
        with(builder) {
            setTitle(getString(R.string.confirm_save_changes_dialog_title))
            setMessage(getString(R.string.confirm_save_changes_dialog_message))
            setPositiveButton(getString(R.string.confirm_save_changes)) { _, _ ->
                updatePropertyIntentPublisher.onNext(UpdatePropertyIntent(newProperty))
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
        if (!resources.getBoolean(R.bool.isMasterDetail)) {
            baseBrowseFragment.detail.requireView().layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
                (this as FrameLayout.LayoutParams).leftMargin = 0
            }
        }
    }

    override fun initializeToolbar() {
        with(baseBrowseFragment.binding.toolBar) {
            title = property.titleInToolbar(resources)
            setNavigationOnClickListener {
                populateChanges()
                if (newProperty != property || newProperty.photos != property.photos) {
                    confirmSaveChanges()
                } else {
                    onBackPressed()
                }
            }
        }
    }

    fun onBackPressed() {
        updateItem.isVisible = false
        onBackPressedCallback.isEnabled = false
        baseBrowseFragment.detail.navController.navigate(
            R.id.navigation_detail,
            bundleOf(
                FROM to arguments?.getString(FROM),
                PROPERTY_ID to property.id
            )
        )
    }

    override fun onUpdateLocationClick() {
        with(binding) {
            street.setText(newProperty.address.street)
            city.setText(newProperty.address.city)
            postalCode.setText(newProperty.address.postalCode)
            country.setText(newProperty.address.country)
            state.setText(newProperty.address.state)
        }
    }
}
