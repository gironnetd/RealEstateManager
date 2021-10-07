package com.openclassrooms.realestatemanager.ui.property.propertydetail

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to display and edit a real estate.
 */
class PropertyDetailFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory, registry: ActivityResultRegistry?)
    : PropertyEditFragment(registry), OnMapReadyCallback, OnMapLoadedCallback,
    BaseBrowseFragment.OnItemClickListener, PhotoDetailAdapter.OnItemClickListener,
    MviView<PropertyDetailIntent, PropertyDetailViewState> {

    private val mainActivity by lazy { activity as FragmentActivity }

    private val propertyDetailViewModel: PropertyDetailViewModel by viewModels { viewModelFactory }

    lateinit var property: Property
    private lateinit var editItem: MenuItem
    private lateinit var searchItem: MenuItem
    private lateinit var mMap: GoogleMap

    private lateinit var innerInflater: LayoutInflater

    private lateinit var detailLayoutParams: FrameLayout.LayoutParams
    private lateinit var browseDetailNavHostFragment: NavHostFragment

    lateinit var detailPhotoAlertDialog: DetailPhotoDialogFragment
    lateinit var detailLocationAlertDialog: DetailLocationDialogFragment

    private val populatePropertyIntentPublisher = PublishSubject.create<PropertyDetailIntent.PopulatePropertyIntent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        baseBrowseFragment.let { baseBrowseFragment ->
            when(baseBrowseFragment::class.java) {
                BrowseResultFragment::class.java -> {
                    innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Tertiary))
                }
                BrowseFragment::class.java -> {
                    innerInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme_Primary))
                }
            }
        }

        _binding = FragmentEditBinding.inflate(inflater, container, false)
        //super.onCreateView(innerInflater, container, savedInstanceState)

        return binding.root
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }*/

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertyDetailViewModel.states().subscribe(this::render))
        propertyDetailViewModel.processIntents(intents())
        //applyDisposition()
        requireArguments().getString(PROPERTY_ID)?.let { propertyId ->
            showDetails(propertyId)
        }
        configureView()
        if(!onBackPressedCallback.isEnabled) {
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
            if(!::property.isInitialized || property != propertyWithPhotos || property.photos != propertyWithPhotos.photos) {
                property = propertyWithPhotos
                properties.value?.let { properties ->
                    properties[properties.indexOf(properties.single { it.id == property.id })] = propertyWithPhotos
                    binding.loadingPhotos.visibility = GONE
                    with((binding.photosRecyclerView.adapter as PhotoDetailAdapter)) {
                        submitList(property.photos)
                    }
                }
            }
        }
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

            addAPhoto!!.visibility = GONE

            entryDate.setOnClickListener(null)
            status.setOnClickListener(null)
            soldDate.setOnClickListener(null)
            type.setOnClickListener(null)

            //price.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))
            //surface.filters = arrayOf<InputFilter>(InputFilterMinMax(0, 99999999999999999))

            PhotoDetailAdapter(innerInflater.context).apply {
                if(property.photos.isNotEmpty()) { noPhotos.visibility = GONE }
                photosRecyclerView.adapter = this
                setOnItemClickListener(this@PropertyDetailFragment)
                submitList(property.photos)
            }

            mapViewButton!!.setImageResource(R.drawable.ic_baseline_location_on_36)

            mapViewButton.setOnClickListener {
                if(!::detailLocationAlertDialog.isInitialized) {
                    detailLocationAlertDialog = DetailLocationDialogFragment(innerContext = innerInflater.context, property)
                    detailLocationAlertDialog.show(childFragmentManager, DetailLocationDialogFragment.TAG)
                } else {
                    detailLocationAlertDialog.property = property
                    detailLocationAlertDialog.alertDialog.show()
                }
            }

            /*if(mapDetailFragment.contentDescription != DETAIL_MAP_FINISH_LOADING) {
                activity?.runOnUiThread {
                    (childFragmentManager.findFragmentById(R.id.map_detail_fragment) as SupportMapFragment)
                        .getMapAsync(this@PropertyDetailFragment)
                }
            } else {
                moveCameraToPropertyInsideMap()
            }*/
        }
    }

    override fun initInterestPoints() {
        with(binding) {
            interestPointsChipGroup.removeAllViewsInLayout()
            if(::property.isInitialized) {
                property.interestPoints.forEach { interestPoint ->
                    if(interestPoint != InterestPoint.NONE) {
                        val newChip = innerInflater.inflate(R.layout.layout_interest_point_chip_default,
                            binding.interestPointsChipGroup, false) as Chip
                        newChip.text = resources.getString(interestPoint.place)
                        newChip.isCheckable = false
                        newChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)

                        interestPointsChipGroup.addView(newChip)
                    }
                }
            }
        }
    }

    private fun displayDetail() {
        baseBrowseFragment.binding.toolBar.title = property.titleInToolbar(resources)

        if(baseBrowseFragment.binding.toolBar.visibility == GONE) {
            baseBrowseFragment.binding.toolBar.visibility = VISIBLE
        }

        if(baseBrowseFragment is BrowseResultFragment) {
            val mainSearchFragment: MainSearchFragment = baseBrowseFragment.parentFragment?.parentFragment as MainSearchFragment

            if(mainSearchFragment.binding.toolBar.visibility == VISIBLE) {
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
            if(photos.isNotEmpty() && photos.none { photo -> !photo.mainPhoto }) {
                binding.loadingPhotos.visibility = VISIBLE
                populatePropertyIntentPublisher.onNext(PropertyDetailIntent.PopulatePropertyIntent(property.id))
            }
            displayDetail()
        }
    }

    /*override fun onPrepareOptionsMenu(menu: Menu) {
        baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search).isVisible = false
        baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_edit).isVisible = true
        super.onPrepareOptionsMenu(baseBrowseFragment.binding.toolBar.menu)
    }*/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(!::editItem.isInitialized) {
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
            if(baseBrowseFragment.detail.childFragmentManager
                    .findFragmentByTag(R.id.navigation_edit.toString()) != null) {

                val propertyUpdateFragment: PropertyUpdateFragment = baseBrowseFragment.detail.childFragmentManager
                    .findFragmentByTag(R.id.navigation_edit.toString()) as PropertyUpdateFragment

                propertyUpdateFragment.showDetails(property.id)
            }
            baseBrowseFragment.detail.findNavController().navigate(R.id.navigation_update, bundle)
        }

        /*editItem.setOnMenuItemClickListener {
            val bundle = bundleOf(FROM to arguments?.getString(FROM), PROPERTY_ID to property.id)
            if(baseBrowseFragment.detail.childFragmentManager
                    .findFragmentByTag(R.id.navigation_edit.toString()) != null) {

                val propertyUpdateFragment: PropertyUpdateFragment = baseBrowseFragment.detail.childFragmentManager
                    .findFragmentByTag(R.id.navigation_edit.toString()) as PropertyUpdateFragment

                propertyUpdateFragment.showDetails(property.id)
                //baseBrowseFragment.detail.findNavController().navigate(R.id.navigation_update, bundle)
            } *//*else {

            }*//*
            baseBrowseFragment.detail.findNavController().navigate(R.id.navigation_edit, bundle)
            true
        }*/

        if(!::searchItem.isInitialized) {
            searchItem = baseBrowseFragment.binding.toolBar.menu.findItem(R.id.navigation_main_search)
        }
        searchItem.isVisible = false

        super.onCreateOptionsMenu(baseBrowseFragment.binding.toolBar.menu, inflater)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            if(::searchItem.isInitialized) { searchItem.isVisible = true }
            if(::editItem.isInitialized) { editItem.isVisible = false }
            binding.editFragment.fullScroll(FOCUS_UP)
            baseBrowseFragment.binding.toolBar.setNavigationOnClickListener(null)
        } else {
            //applyDisposition()
            requireArguments().getString(PROPERTY_ID)?.let { propertyId ->
                showDetails(propertyId)
            }
            configureView()
            onBackPressedCallback.isEnabled = true
        }
    }

    override fun onItemClick(propertyId: String) {
        showDetails(propertyId = propertyId)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //applyDisposition()
    }

    private fun applyDisposition() {
        screenWidth = screenWidth(requireActivity())

        this.parentFragment?.let {
            browseDetailNavHostFragment = this.parentFragment as NavHostFragment

            detailLayoutParams = (browseDetailNavHostFragment.requireView().layoutParams
                ?: FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT))
                    as FrameLayout.LayoutParams

            if (resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp >= 600
                && resources.configuration.smallestScreenWidthDp < 720
            ) {
                applyMasterDetailDisposition()
            } else if((resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp > 720
                        && resources.configuration.orientation == ORIENTATION_LANDSCAPE)) {
                applyMasterDetailDisposition()
            } else if((resources.getBoolean(R.bool.isMasterDetail) && resources.configuration.smallestScreenWidthDp > 720
                        && resources.configuration.orientation == ORIENTATION_PORTRAIT)) {
                applyNormalDisposition()
            }
            else if (!resources.getBoolean(R.bool.isMasterDetail)) {
                applyNormalDisposition()
            }
        }
    }

    private fun applyNormalDisposition() {
        if(resources.configuration.smallestScreenWidthDp > 720) {
            resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
            resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)

            detailLayoutParams.apply {
                width = (screenWidth * detailWidthWeight.float).toInt()
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                leftMargin = (screenWidth * masterWidthWeight.float).toInt()
            }.also { layoutParams ->
                browseDetailNavHostFragment.requireView().layoutParams = layoutParams
            }
        } else {

            detailLayoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
                leftMargin = 0
            }.also { layoutParams ->
                browseDetailNavHostFragment.requireView().layoutParams = layoutParams
            }
        }

        /*(binding.layoutPropertyAddress!!.layoutParams as ConstraintLayout.LayoutParams).apply {
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            endToStart = ConstraintLayout.LayoutParams.UNSET
            bottomToTop = binding.mapDetailFragment.id
            horizontalWeight = 1f
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            leftMargin = 0
            rightMargin = 0
        }.also { addressLayoutParams ->
            binding.layoutPropertyAddress!!.layoutParams = addressLayoutParams

            (binding.cityTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 0.5f
                endToStart = binding.postalCodeTextInputLayout.id
                endToEnd = ConstraintLayout.LayoutParams.UNSET
                topMargin = 8
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 8
            }.also { layoutParams ->
                binding.cityTextInputLayout.layoutParams = layoutParams
            }

            (binding.postalCodeTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 0.5f

                topToBottom = binding.streetTextInputLayout.id
                startToEnd = binding.cityTextInputLayout.id
                startToStart = ConstraintLayout.LayoutParams.UNSET
                topMargin = 8
                leftMargin = 8
                bottomMargin = 0
                rightMargin = 0
            }.also { layoutParams ->
                binding.postalCodeTextInputLayout.layoutParams = layoutParams
            }

            (binding.countryTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 0.5f
                endToStart = binding.stateTextInputLayout.id
                endToEnd = ConstraintLayout.LayoutParams.UNSET
                topToBottom = binding.cityTextInputLayout.id
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                topMargin = 8
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 8
            }.also { layoutParams ->
                binding.countryTextInputLayout.layoutParams = layoutParams
            }

            (binding.stateTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 0.5f
                topToBottom = binding.postalCodeTextInputLayout.id
                startToEnd = binding.countryTextInputLayout.id
                startToStart = ConstraintLayout.LayoutParams.UNSET
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                topMargin = 8
                leftMargin = 8
                bottomMargin = 16
                rightMargin = 0
            }.also { layoutParams ->
                binding.stateTextInputLayout.layoutParams = layoutParams
            }
        }
*/
        /*(binding.mapDetailFragment.layoutParams as ConstraintLayout.LayoutParams).apply {

            val containerLayoutParams = binding.container.layoutParams as FrameLayout.LayoutParams

            leftMargin = 16
            rightMargin = 16
            topMargin = 0
            bottomMargin = 16

            width = screenWidth(requireActivity())
            width -= containerLayoutParams.leftMargin + leftMargin
            width -= containerLayoutParams.rightMargin + rightMargin
            height = width

            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            startToEnd = ConstraintLayout.LayoutParams.UNSET
            //topToBottom = binding.layoutPropertyAddress!!.id
            topToTop = ConstraintLayout.LayoutParams.UNSET
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            horizontalWeight = 1f
        }.also { layoutParams ->
            binding.mapDetailFragment.layoutParams = layoutParams
        }*/
    }

    private fun applyMasterDetailDisposition() {

        resources.getValue(R.dimen.detail_width_weight, detailWidthWeight, false)
        resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)

        detailLayoutParams.apply {
            width = (screenWidth * detailWidthWeight.float).toInt()
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            leftMargin = (screenWidth * masterWidthWeight.float).toInt()
        }.also { layoutParams ->
            browseDetailNavHostFragment.requireView().layoutParams = layoutParams
        }

        /*(binding.layoutPropertyAddress!!.layoutParams as ConstraintLayout.LayoutParams).apply {
            endToEnd = ConstraintLayout.LayoutParams.UNSET
            endToStart = binding.mapDetailFragment.id
            bottomToTop = ConstraintLayout.LayoutParams.UNSET
            horizontalWeight = 0.5f
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            setMargins( 16, 0, 8, 0)
        }.also { addressLayoutParams ->
            binding.layoutPropertyAddress!!.layoutParams = addressLayoutParams

            (binding.cityTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 1f
                endToStart = ConstraintLayout.LayoutParams.UNSET
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 8
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }.also { layoutParams ->
                binding.cityTextInputLayout.layoutParams = layoutParams
            }

            (binding.postalCodeTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 1f
                topToBottom = binding.cityTextInputLayout.id
                startToEnd = ConstraintLayout.LayoutParams.UNSET
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 8
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }.also { layoutParams ->
                binding.postalCodeTextInputLayout.layoutParams = layoutParams
            }

            (binding.countryTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 1f
                endToStart = ConstraintLayout.LayoutParams.UNSET
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = binding.postalCodeTextInputLayout.id
                topMargin = 8
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }.also { layoutParams ->
                binding.countryTextInputLayout.layoutParams = layoutParams
            }

            (binding.stateTextInputLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                horizontalWeight = 1f
                topToBottom = binding.countryTextInputLayout.id
                startToEnd = ConstraintLayout.LayoutParams.UNSET
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 8
                leftMargin = 0
                bottomMargin = 16
                rightMargin = 0
            }.also { layoutParams ->
                binding.stateTextInputLayout.layoutParams = layoutParams
            }
        }
*/
        /*(binding.mapDetailFragment.layoutParams as ConstraintLayout.LayoutParams).apply {
            width = 0
            height = 0
            startToStart = ConstraintLayout.LayoutParams.UNSET
            //startToEnd = binding.layoutPropertyAddress!!.id
            topToBottom = ConstraintLayout.LayoutParams.UNSET
            //topToTop = binding.layoutPropertyAddress!!.id
            //bottomToBottom = binding.layoutPropertyAddress!!.id
            horizontalWeight = 0.5f
            setMargins( 8, 16, 16, 16)
        }.also { layoutParams ->
            binding.mapDetailFragment.layoutParams = layoutParams
        }*/
    }

    override fun confirmSaveChanges() {}

    override fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { onBackPressed() }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun layoutInflater(): LayoutInflater {
        return innerInflater
    }

    override fun initializeToolbar() {}

    private fun onBackPressed() {
        if(baseBrowseFragment is BrowseResultFragment) {
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
        if (resources.getBoolean(R.bool.isMasterDetail)) {
            masterDetailOnBackPressed()
        } else {
            normalOnBackPressed()
        }
    }

    private fun masterDetailOnBackPressed() {
        when(arguments?.getString(FROM)) {
            BaseMapFragment::class.java.name -> {
                baseBrowseFragment
                    .detail
                    .navController
                    .navigate(R.id.navigation_map)
            }
        }
    }

    private fun normalOnBackPressed() {
        when(arguments?.getString(FROM)) {
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
                    binding.segmentedcontrol.listViewButton.isSelected = false
                    binding.segmentedcontrol.mapViewButton.isSelected = true
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) { mMap = googleMap
        /*
        Deprecation notice: In a future release, indoor will no longer be supported on satellite,
        hybrid or terrain type maps. Even where indoor is not supported, isIndoorEnabled()
        will continue to return the value that has been set via setIndoorEnabled(),
        as it does now. By default, setIndoorEnabled is 'true'.
        The API release notes (https://developers.google.com/maps/documentation/android-api/releases)
        will let you know when indoor support becomes unavailable on those map types.
        */

        mMap.isIndoorEnabled = false
        moveCameraToPropertyInsideMap()
        val options = GoogleMapOptions().liteMode(true)
        mMap.mapType = options.mapType
        //binding.mapDetailFragment.contentDescription = DETAIL_MAP_NOT_FINISH_LOADING
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        //binding.mapDetailFragment.contentDescription = DETAIL_MAP_FINISH_LOADING
    }

    private fun moveCameraToPropertyInsideMap() {
        mMap.clear()

        if(!property.address.latitude.equals(0.0) && !property.address.longitude.equals(0.0)) {
            mMap.addMarker(MarkerOptions()
                .position(LatLng(property.address.latitude,
                    property.address.longitude)
                ).icon(BitmapUtil.bitmapDescriptorFromVector(innerInflater.context, R.drawable.ic_marker_selected))
            )

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(property.address.latitude,
                    property.address.longitude), (DEFAULT_ZOOM + 3))

            mMap.moveCamera(cameraUpdate)
        }
    }

    companion object {
        const val DETAIL_MAP_NOT_FINISH_LOADING = "detail_map_not_finish_loading"
        const val DETAIL_MAP_FINISH_LOADING = "detail_map_finish_loading"
    }

    override fun clickOnPhotoAtPosition(photoId: String) {
        detailPhotoAlertDialog = DetailPhotoDialogFragment().also {
            it.photo = property.photos.singleOrNull { photo -> photo.id == photoId }
        }
        detailPhotoAlertDialog.show(childFragmentManager, DetailPhotoDialogFragment.TAG)
    }
}