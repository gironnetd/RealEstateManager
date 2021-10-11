package com.openclassrooms.realestatemanager.ui.property.edit.create

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyCreateIntent.InitialIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.EditLocationDialogFragment.Companion.TAG
import com.openclassrooms.realestatemanager.ui.property.edit.dialog.location.add.AddLocationDialogFragment
import com.openclassrooms.realestatemanager.util.AppNotificationManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to Create a real estate.
 */
class PropertyCreateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?) : PropertyEditFragment(registry),
    MviView<PropertyEditIntent.PropertyCreateIntent, PropertyEditViewState>
    ,  AddLocationDialogFragment.AddLocationListener {

    private val propertyCreateViewModel: PropertyCreateViewModel by viewModels { viewModelFactory }

    val mainActivity by lazy { activity as MainActivity }

    private lateinit var createItem: MenuItem
    private lateinit var searchItem: MenuItem

    private lateinit var innerInflater: LayoutInflater

    private val createPropertyIntentPublisher = PublishSubject.create<CreatePropertyIntent>()

    lateinit var addLocationAlertDialog: AddLocationDialogFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Secondary))
        _binding = FragmentEditBinding.inflate(innerInflater, container, false)
        super.onCreateView(innerInflater, container, savedInstanceState)
        return binding.root
    }

    override fun configureView() {
        super.configureView()
        with(binding) {
            description.minLines = 4
            mapViewButton!!.setImageResource(R.drawable.ic_baseline_add_location_36)

            mapViewButton.setOnClickListener {
                if(!::addLocationAlertDialog.isInitialized) {
                    addLocationAlertDialog = AddLocationDialogFragment(innerContext = innerInflater.context)
                    addLocationAlertDialog.show(childFragmentManager, TAG)
                    addLocationAlertDialog.setCallBack(this@PropertyCreateFragment)
                } else {
                    addLocationAlertDialog.address = newProperty.address
                    addLocationAlertDialog.alertDialog.show()
                }
            }

            street.onFocusChangeListener = null
            city.onFocusChangeListener = null
            postalCode.onFocusChangeListener = null
            country.onFocusChangeListener = null
            state.onFocusChangeListener = null
        }
        //binding.mapDetailFragment.visibility = GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        createItem = menu.findItem(R.id.navigation_create)
        createItem.isVisible = true

        // getting Linear Layout from custom layout
        val createItemLayout = createItem.actionView as LinearLayout

        createItemLayout.apply {
            //findViewById<ImageView>(R.id.menu_item_icon).setImageResource(null)
            findViewById<TextView>(R.id.menu_item_title).text = resources.getString(R.string.create)
        }

        createItemLayout.setOnClickListener {
            populateChanges()
            if(newProperty != Property() || newProperty.photos.isNotEmpty()) {
                confirmSaveChanges()
            } else {
                showMessage(resources.getString(R.string.no_changes))
            }
        }

        searchItem = menu.findItem(R.id.navigation_main_search)
        searchItem.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_create -> {
                populateChanges()
                if(newProperty != Property() || newProperty.photos.isNotEmpty()) {
                    confirmSaveChanges()
                } else {
                    showMessage(resources.getString(R.string.no_changes))
                }
                return true
            }
            R.id.navigation_main_search -> {
                (activity as MainActivity).navController.navigate(R.id.navigation_main_search)
                return true
            }
            else -> { return true }
        }
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyCreateViewModel.states().subscribe(this::render))
        propertyCreateViewModel.processIntents(intents())
        configureView()
    }

    override fun initializeToolbar() {
        mainActivity.binding.toolBar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSecondary, null))
        mainActivity.binding.statusbar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSecondaryDark, null))
            with(mainActivity) {
                if(binding.toolBar.visibility == GONE) {
                    binding.toolBar.visibility = VISIBLE
                }
            }
    }

    override fun intents(): Observable<PropertyEditIntent.PropertyCreateIntent> {
        return Observable.merge(initialIntent(), loadPropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<InitialIntent> {
        return Observable.just(InitialIntent)
    }

    private fun loadPropertyIntentPublisher(): Observable<CreatePropertyIntent> {
        return createPropertyIntentPublisher
    }

    override fun render(state: PropertyEditViewState) {
        if(state.isSaved) {
            state.uiNotification?.let { uiNotification ->

                val mNotificationManager = AppNotificationManager(requireActivity())

                if(uiNotification == PropertyEditViewState.UiNotification.PROPERTIES_FULLY_CREATED) {
                    mNotificationManager.showNotification(newProperty, resources.getString(R.string.property_create_totally))
                }

                if(uiNotification == PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_CREATED) {
                    mNotificationManager.showNotification(newProperty, resources.getString(R.string.property_create_locally))
                }
            }

            properties.value?.let {
                it.add(newProperty)
                properties.value = it
            }

            onBackPressed()
        }
    }

    override fun confirmSaveChanges() {
        val builder = AlertDialog.Builder(innerInflater.context)
        with(builder) {
            setTitle(getString(R.string.confirm_create_changes_dialog_title))
            setMessage(getString(R.string.confirm_create_changes_dialog_message))
            setPositiveButton(getString(R.string.confirm_create_changes))  { _, _ ->
                if(newProperty.photos.any { photo -> photo.locallyDeleted }) {
                    newProperty.photos.removeAll(newProperty.photos.filter { photo -> photo.locallyDeleted })
                }
                createPropertyIntentPublisher.onNext(CreatePropertyIntent(newProperty))
            }
            setNegativeButton(getString(R.string.no)) { _, _ -> onBackPressed() }
            show()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(hidden) {
            mainActivity.binding.toolBar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSecondary, null))
            mainActivity.binding.statusbar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorSecondaryDark, null))
            newProperty = Property()
            createItem.isVisible = false
            searchItem.isVisible = true
            onBackPressedCallback.isEnabled = true
            clearView()
        } else {
            initializeToolbar()
            createItem.isVisible = true
            searchItem.isVisible = false
            onBackPressedCallback.isEnabled = false
        }
    }

    fun onBackPressed() {
        (activity as MainActivity).navController.navigate(R.id.navigation_browse)
    }

    override fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                populateChanges()
                if(newProperty != Property() || newProperty.photos != mutableListOf<Photo>()) {
                    confirmSaveChanges()
                } else {
                    onBackPressed()
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun layoutInflater(): LayoutInflater {
        return innerInflater
    }

    override fun onAddLocationClick() {
        with(binding) {
            street.setText(newProperty.address.street)
            city.setText(newProperty.address.city)
            postalCode.setText(newProperty.address.postalCode)
            country.setText(newProperty.address.country)
            state.setText(newProperty.address.state)
        }
    }
}