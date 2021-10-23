package com.openclassrooms.realestatemanager.ui.property.propertydetail

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView.FOCUS_UP
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.property.InterestPoint
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.dialog.location.DetailLocationDialogFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.dialog.photo.DetailPhotoDialogFragment
import com.openclassrooms.realestatemanager.ui.property.search.MainSearchFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseBrowseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.list.BaseListFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to display and edit a real estate.
 */
class PropertyDetailFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?) :
    PropertyEditFragment(registry),
    BaseBrowseFragment.OnItemClickListener,
    PhotoDetailAdapter.OnItemClickListener,
    MviView<PropertyDetailIntent, PropertyDetailViewState> {

    private val mainActivity by lazy { activity as FragmentActivity }

    private val propertyDetailViewModel: PropertyDetailViewModel by viewModels { viewModelFactory }

    lateinit var property: Property
    private lateinit var editItem: MenuItem
    private lateinit var updateItem: MenuItem
    private lateinit var searchItem: MenuItem

    private lateinit var innerInflater: LayoutInflater

    lateinit var detailPhotoAlertDialog: DetailPhotoDialogFragment
    private lateinit var detailLocationAlertDialog: DetailLocationDialogFragment

    private val populatePropertyIntentPublisher = PublishSubject.create<PropertyDetailIntent.PopulatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        baseBrowseFragment.let { baseBrowseFragment ->
            when (baseBrowseFragment::class.java) {
                BrowseResultFragment::class.java -> {
                    innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Tertiary))
                }
                BrowseFragment::class.java -> {
                    innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Primary))
                }
            }
        }

        editBinding = FragmentEditBinding.inflate(innerInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyDetailViewModel.states().subscribe(this::render))
        propertyDetailViewModel.processIntents(intents())
        requireArguments().getString(PROPERTY_ID)?.let { propertyId ->
            showDetails(propertyId)
        }
        configureView()
        if (!onBackPressedCallback.isEnabled) {
            onBackPressedCallback.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    override fun intents(): Observable<PropertyDetailIntent> {
        return Observable.merge(initialIntent(), populatePropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertyDetailIntent> {
        return Observable.just(PropertyDetailIntent.InitialIntent)
    }

    private fun populatePropertyIntentPublisher(): Observable<PropertyDetailIntent.PopulatePropertyIntent> {
        return populatePropertyIntentPublisher
    }

    override fun render(state: PropertyDetailViewState) {
        state.property?.let { propertyWithPhotos ->
            if (!::property.isInitialized || property != propertyWithPhotos ||
                property.photos != propertyWithPhotos.photos
            ) {
                property = propertyWithPhotos
                properties.value?.let { properties ->
                    properties[properties.indexOf(properties.single { it.id == property.id })] = propertyWithPhotos
                    with((binding.photosRecyclerView.adapter as PhotoDetailAdapter)) {
                        submitList(property.photos)
                    }
                }
            }
        }
        binding.loadingPhotos.visibility = GONE
    }

    override fun configureView() {
        super.configureView()
        with(binding) {
            description.isFocusable = false
            entryDate.isFocusable = false
            status.isFocusable = false
            soldDate.isFocusable = false
            price.isFocusable = false
            type.isFocusable = false
            surface.isFocusable = false
            rooms.isFocusable = false
            bedrooms.isFocusable = false
            bathrooms.isFocusable = false
            street.isFocusable = false
            city.isFocusable = false
            postalCode.isFocusable = false
            country.isFocusable = false
            state.isFocusable = false

            addAPhoto.visibility = GONE

            entryDate.setOnClickListener(null)
            status.setOnClickListener(null)
            soldDate.setOnClickListener(null)
            type.setOnClickListener(null)

            PhotoDetailAdapter(innerInflater.context).apply {
                if (property.photos.isNotEmpty()) { noPhotos.visibility = GONE }
                photosRecyclerView.adapter = this
                setOnItemClickListener(this@PropertyDetailFragment)
                submitList(property.photos)
            }

            mapViewButton.setImageResource(R.drawable.ic_baseline_location_on_36)

            mapViewButton.setOnClickListener {
                if (!::detailLocationAlertDialog.isInitialized) {
                    detailLocationAlertDialog = DetailLocationDialogFragment(
                        innerContext = innerInflater.context, property
                    )
                    detailLocationAlertDialog.show(childFragmentManager, DetailLocationDialogFragment.TAG)
                } else {
                    detailLocationAlertDialog.property = property
                    detailLocationAlertDialog.alertDialog.show()
                }
            }
        }
    }

    override fun initInterestPoints() {
        with(binding) {
            interestPointsChipGroup.removeAllViewsInLayout()
            if (::property.isInitialized) {
                property.interestPoints.forEach { interestPoint ->
                    if (interestPoint != InterestPoint.NONE) {
                        val newChip = innerInflater.inflate(
                            R.layout.layout_interest_point_chip_default,
                            binding.interestPointsChipGroup, false
                        ) as Chip
                        newChip.text = resources.getString(interestPoint.place)
                        newChip.isCheckable = false

                        interestPointsChipGroup.addView(newChip)
                    }
                }
            }
        }
    }

    private fun displayDetail() {
        baseBrowseFragment.binding.toolBar.title = property.titleInToolbar(resources)

        if (baseBrowseFragment.binding.toolBar.visibility == GONE) {
            baseBrowseFragment.binding.toolBar.visibility = VISIBLE
        }

        if (baseBrowseFragment is BrowseResultFragment) {
            val mainSearchFragment: MainSearchFragment = baseBrowseFragment.parentFragment?.parentFragment
                as MainSearchFragment

            if (mainSearchFragment.binding.toolBar.visibility == VISIBLE) {
                mainSearchFragment.binding.toolBar.visibility = GONE
            }
        }

        with(baseBrowseFragment.binding.toolBar) {
            setNavigationOnClickListener {
                onBackPressed()
                onBackPressedCallback.isEnabled = false
            }
        }
        baseBrowseFragment.setOnItemClickListener(this)
    }

    fun showDetails(propertyId: String) {
        properties.value?.let { properties ->
            property = properties.single { property -> property.id == propertyId }
            newProperty = property.deepCopy()
            val photos = properties.single { property -> property.id == propertyId }.photos
            if (photos.isNotEmpty() && photos.none { photo -> !photo.mainPhoto }) {
                binding.loadingPhotos.visibility = VISIBLE
                populatePropertyIntentPublisher.onNext(PropertyDetailIntent.PopulatePropertyIntent(property.id))
            }
            displayDetail()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!::editItem.isInitialized) {
            editItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_edit)
        }
        editItem.isVisible = true

        // getting Linear Layout from custom layout
        val editItemLayout = editItem.actionView as LinearLayout

        editItemLayout.apply {
            findViewById<ImageView>(R.id.menu_item_icon).setImageResource(R.drawable.ic_baseline_edit_24)
            findViewById<TextView>(R.id.menu_item_title).text = resources.getString(R.string.edit)
        }

        editItemLayout.setOnClickListener {
            val bundle = bundleOf(FROM to arguments?.getString(FROM), PROPERTY_ID to property.id)
            if (baseBrowseFragment.detail.childFragmentManager
                .findFragmentByTag(R.id.navigation_update.toString()) != null
            ) {
                val propertyUpdateFragment: PropertyUpdateFragment = baseBrowseFragment.detail.childFragmentManager
                    .findFragmentByTag(R.id.navigation_update.toString()) as PropertyUpdateFragment

                propertyUpdateFragment.showDetails(property.id)
            }
            baseBrowseFragment.detail.findNavController().navigate(R.id.navigation_update, bundle)
        }

        if (!::searchItem.isInitialized) {
            searchItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search)
        }
        searchItem.isVisible = false

        if (!::updateItem.isInitialized) {
            updateItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_update)
        }
        updateItem.isVisible = false

        super.onCreateOptionsMenu(baseBrowseFragment.binding.toolBar.menu, inflater)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            if (::searchItem.isInitialized) { searchItem.isVisible = true }
            if (::editItem.isInitialized) { editItem.isVisible = false }
            binding.editFragment.fullScroll(FOCUS_UP)
            baseBrowseFragment.binding.toolBar.setNavigationOnClickListener(null)
            onBackPressedCallback.isEnabled = false
        } else {
            requireArguments().getString(PROPERTY_ID)?.let { propertyId ->
                showDetails(propertyId)
            }
            configureView()
            onBackPressedCallback.isEnabled = true
            updateItem.isVisible = false
        }
    }

    override fun onItemClick(propertyId: String) {
        showDetails(propertyId = propertyId)
        configureView()
    }

    override fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { onBackPressed() }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun layoutInflater(): LayoutInflater {
        return innerInflater
    }

    internal fun onBackPressed() {
        if (baseBrowseFragment is BrowseResultFragment) {
            baseBrowseFragment.binding.toolBar.visibility = GONE
            baseBrowseFragment.parentFragment?.let {
                (it.parentFragment as MainSearchFragment).binding.toolBar.visibility = VISIBLE
            }
        } else {
            baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search).isVisible = true
            baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_edit).isVisible = false
            baseBrowseFragment.binding.toolBar.visibility = GONE

            (mainActivity as? MainActivity)?.let { mainActivity ->
                mainActivity.binding.toolBar.visibility = VISIBLE
            }
        }

        onBackPressedCallback.isEnabled = false

        if (baseBrowseFragment is BrowseResultFragment) {
            (baseBrowseFragment as BrowseResultFragment).mainSearchFragment.onBackPressedCallback.isEnabled = true
        }

        if (resources.getBoolean(R.bool.isMasterDetail)) {
            masterDetailOnBackPressed()
        } else {
            normalOnBackPressed()
        }
    }

    private fun masterDetailOnBackPressed() {
        baseBrowseFragment.detail.navController.navigate(R.id.navigation_map)
    }

    private fun normalOnBackPressed() {
        when (arguments?.getString(FROM)) {
            BaseListFragment::class.java.name -> {
                baseBrowseFragment.apply {
                    master.requireView().visibility = VISIBLE
                    binding.resultListFragment.visibility = VISIBLE
                    binding.resultListFragment.bringToFront()
                    detail.requireView().visibility = GONE
                    binding.resultDetailNavFragment.visibility = GONE
                    detail.navController.navigate(R.id.navigation_map)
                    binding.segmentedcontrol.buttonContainer.visibility = VISIBLE
                    binding.segmentedcontrol.listViewButton.isSelected = true
                    binding.segmentedcontrol.mapViewButton.isSelected = false
                }
            }
            BaseMapFragment::class.java.name -> {
                baseBrowseFragment.apply {
                    detail.navController.navigate(R.id.navigation_map)
                    master.requireView().visibility = GONE
                    binding.resultListFragment.visibility = GONE
                    detail.requireView().visibility = VISIBLE
                    binding.resultDetailNavFragment.visibility = VISIBLE
                    binding.resultDetailNavFragment.bringToFront()
                    binding.segmentedcontrol.buttonContainer.visibility = VISIBLE
                    binding.segmentedcontrol.root.bringToFront()
                    binding.segmentedcontrol.listViewButton.isSelected = false
                    binding.segmentedcontrol.mapViewButton.isSelected = true
                }
            }
        }
    }

    override fun clickOnPhotoAtPosition(photoId: String) {
        detailPhotoAlertDialog = DetailPhotoDialogFragment().also {
            it.photo = property.photos.singleOrNull { photo -> photo.id == photoId }
        }
        detailPhotoAlertDialog.show(childFragmentManager, DetailPhotoDialogFragment.TAG)
    }
}
