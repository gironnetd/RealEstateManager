package com.openclassrooms.realestatemanager.ui.property.edit.create

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent.PropertyCreateIntent.InitialIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.util.AppNotificationManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to Create a real estate.
 */
class PropertyCreateFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?) : PropertyEditFragment(registry),
    MviView<PropertyEditIntent.PropertyCreateIntent, PropertyEditViewState> {

    private val propertyCreateViewModel: PropertyCreateViewModel by viewModels { viewModelFactory }

    var mainActivity: MainActivity? = null

    private lateinit var createItem: MenuItem

    private val createPropertyIntentPublisher = PublishSubject.create<CreatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        mainActivity = if(activity is MainActivity) { activity as MainActivity }
        else { null }

        return binding.root
    }

    override fun configureView() {
        super.configureView()
        binding.description.minLines = 4
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        createItem = menu.findItem(R.id.navigation_create)
        createItem.isVisible = true
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyCreateViewModel.states().subscribe(this::render))
        propertyCreateViewModel.processIntents(intents())
    }

    override fun initializeToolbar() {
        mainActivity?.let { mainActivity ->
            with(mainActivity) {
                binding.toolBar.visibility = VISIBLE
                setSupportActionBar(binding.toolBar)
                binding.toolBar.setupWithNavController(navController, appBarConfiguration)
                binding.toolBar.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.navigation_create -> {
                            populateChanges()
                            if(newProperty != Property() || newProperty.photos.isNotEmpty()) {
                                confirmSaveChanges()
                            } else {
                                showMessage(resources.getString(R.string.no_changes))
                            }
                        }
                    }
                    super.onOptionsItemSelected(item)
                }
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
        val builder = AlertDialog.Builder(requireContext())
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
            newProperty = Property()
            mainActivity?.binding?.toolBar?.visibility = GONE
            createItem.isVisible = false
            onBackPressedCallback.isEnabled = true
            clearView()
        } else {
            createItem.isVisible = true
            onBackPressedCallback.isEnabled = false
        }
    }

    fun onBackPressed() {
        (activity as MainActivity).navController.navigate(R.id.navigation_real_estate)
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
}