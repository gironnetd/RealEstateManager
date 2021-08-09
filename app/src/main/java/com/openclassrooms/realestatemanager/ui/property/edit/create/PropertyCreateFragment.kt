package com.openclassrooms.realestatemanager.ui.property.edit.create

import android.graphics.Color
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
import com.google.android.material.textfield.TextInputEditText
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.update.PhotoUpdateAdapter
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

    lateinit var mainActivity: MainActivity

    private lateinit var createItem: MenuItem

    private val createPropertyIntentPublisher =
        PublishSubject.create<PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)

        mainActivity = activity as MainActivity

        configureView()
        onBackPressedCallback()

        return binding.root
    }

    private fun configureView() {
        with(binding) {

            val none = resources.getString(R.string.none)
            val colorPrimaryDark: Int = resources.getColor(R.color.colorPrimaryDark)
            description.minLines = 4
            description.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        description.setText("")
                        description.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        description.setText(none)
                        description.setTextColor(colorPrimaryDark)
                    }
                }
            }

            initInterestPoints()

            //price.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            price.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        price.setText("")
                        price.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        price.setText(none)
                        price.setTextColor(colorPrimaryDark)
                    }
                }
            }

            //surface.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            surface.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        surface.setText("")
                        surface.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        surface.setText(none)
                        surface.setTextColor(colorPrimaryDark)
                    }
                }
            }

            rooms.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        rooms.setText("")
                        rooms.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        rooms.setText(none)
                        rooms.setTextColor(colorPrimaryDark)
                    }
                }
            }

            bedrooms.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        bedrooms.setText("")
                        bedrooms.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        bedrooms.setText(none)
                        bedrooms.setTextColor(colorPrimaryDark)
                    }
                }
            }

            bathrooms.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        bathrooms.setText("")
                        bathrooms.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        bathrooms.setText(none)
                        bathrooms.setTextColor(colorPrimaryDark)
                    }
                }
            }

            street.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        street.setText("")
                        street.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        street.setText(none)
                        street.setTextColor(colorPrimaryDark)
                    }
                }
            }

            city.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        city.setText("")
                        city.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        city.setText(none)
                        city.setTextColor(colorPrimaryDark)
                    }
                }
            }

            postalCode.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        postalCode.setText("")
                        postalCode.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        postalCode.setText(none)
                        postalCode.setTextColor(colorPrimaryDark)
                    }
                }
            }

            country.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        country.setText("")
                        country.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        country.setText(none)
                        country.setTextColor(colorPrimaryDark)
                    }
                }
            }

            state.setOnFocusChangeListener { view, hasFocus ->
                with((view as TextInputEditText).text.toString()) {
                    if(hasFocus && this == none) {
                        state.setText("")
                        state.setTextColor(Color.BLACK)
                    }
                    if(!hasFocus && this == "") {
                        state.setText(none)
                        state.setTextColor(colorPrimaryDark)
                    }
                }
            }
        }

        PhotoUpdateAdapter().apply {
            binding.photosRecyclerView.adapter = this
            setOnItemClickListener(this@PropertyCreateFragment)
            submitList(newProperty.photos)
            notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        createItem = menu.findItem(R.id.navigation_create)
        createItem.isVisible = true
    }

    override fun confirmSaveChanges() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(getString(R.string.confirm_create_changes_dialog_title))
            setMessage(getString(R.string.confirm_create_changes_dialog_message))
            setPositiveButton(getString(R.string.confirm_create_changes))  { _, _ ->
            }
            setNegativeButton(getString(R.string.no)) { _, _ -> onBackPressed() }
            show()
        }
    }

    private fun onBackPressed() {
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(hidden) {
            mainActivity.binding.toolBar.visibility = GONE
            createItem.isVisible = false
        } else {
            createItem.isVisible = true
        }
    }

    override fun initializeToolbar() {
        with(mainActivity) {
            binding.toolBar.visibility = VISIBLE
            setSupportActionBar(binding.toolBar)
            binding.toolBar.setupWithNavController(navController, appBarConfiguration)
            binding.toolBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.navigation_create -> {
                    }
                }
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun intents(): Observable<PropertyEditIntent.PropertyCreateIntent> {
        return Observable.merge(initialIntent(), loadPropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertyEditIntent.PropertyCreateIntent.InitialIntent> {
        return Observable.just(PropertyEditIntent.PropertyCreateIntent.InitialIntent)
    }

    private fun loadPropertyIntentPublisher(): Observable<PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent> {
        return createPropertyIntentPublisher
    }

    override fun render(state: PropertyEditViewState) {

    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyCreateViewModel.states().subscribe(this::render))
        propertyCreateViewModel.processIntents(intents())
    }
}