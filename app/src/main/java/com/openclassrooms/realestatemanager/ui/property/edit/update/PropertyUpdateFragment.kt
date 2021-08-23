package com.openclassrooms.realestatemanager.ui.property.edit.update

import android.content.res.Configuration
import android.os.Bundle
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
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent.InitialIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_UPDATED
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to edit and update a real estate.
 */
class PropertyUpdateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?)
    : PropertyEditFragment(registry), /*PropertyDetailFragment.OnItemClickListener,*/
    MviView<PropertyUpdateIntent, PropertyEditViewState> {

    private val propertyUpdateViewModel: PropertyUpdateViewModel by viewModels { viewModelFactory }

    lateinit var property: Property
    private lateinit var updateItem: MenuItem

    private val updatePropertyIntentPublisher = PublishSubject.create<UpdatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle? ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        properties.value?.let { properties ->
            property = properties.single { property -> property.id == arguments?.getString(PROPERTY_ID) }
            newProperty = property.deepCopy()
        }
        onBackPressedCallback()
        applyDisposition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compositeDisposable.add(propertyUpdateViewModel.states().subscribe(this::render))
        propertyUpdateViewModel.processIntents(intents())
        showDetails(property.id)
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

        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.binding.toolBar.title = property.titleInToolbar(resources)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            this.parentFragment?.parentFragment?.let {
                val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
                browseFragment.binding.toolBar.setNavigationOnClickListener(null)
            }
            onBackPressedCallback.isEnabled = false
            updateItem.isVisible = false
            clearView()
        } else {
            updateItem.isVisible = true
            onBackPressedCallback.isEnabled = true
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
                title = property.titleInToolbar(resources)
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
}

