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
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColorStateList
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyStatus
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListAdapter
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment.Companion.DEFAULT_ZOOM
import com.openclassrooms.realestatemanager.ui.property.propertydetail.view.PhotoDetailDialogFragment
import com.openclassrooms.realestatemanager.ui.property.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID
import com.openclassrooms.realestatemanager.util.Utils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to display and edit a real estate.
 */
class PropertyDetailFragment
@Inject constructor(viewModelFactory: ViewModelProvider.Factory)
    : BaseFragment(R.layout.fragment_detail), OnMapReadyCallback,
    GoogleMap.OnMapLoadedCallback, BrowseFragment.OnItemClickListener,
    PhotoDetailAdapter.OnItemClickListener, MviView<PropertyDetailIntent, PropertyDetailViewState> {

    private val propertyDetailViewModel: PropertyDetailViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentDetailBinding? = null
    val binding get() = _binding!!

    lateinit var propertyId: String
    lateinit var property: Property
    private lateinit var editItem: MenuItem
    private lateinit var mMap: GoogleMap
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var detailLayoutParams: FrameLayout.LayoutParams
    private lateinit var browseDetailNavHostFragment: NavHostFragment

    lateinit var detailPhotoAlertDialog: PhotoDetailDialogFragment

    interface OnItemClickListener {
        fun onItemClick(propertyId: String)
    }

    private var callBack: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) { callBack = listener }

    private val populatePropertyIntentPublisher =
        PublishSubject.create<PropertyDetailIntent.PopulatePropertyIntent>()
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        onBackPressedCallback()

        arguments?.let { arguments ->
            propertyId = arguments.getString(PROPERTY_ID).toString()
        }

        applyDisposition()
        return binding.root
    }

    override fun intents(): Observable<PropertyDetailIntent> {
        return Observable.merge(initialIntent(), populatePropertyIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertyDetailIntent.InitialIntent> {
        return Observable.just(PropertyDetailIntent.InitialIntent(propertyId))
    }

    private fun populatePropertyIntentPublisher(): Observable<PropertyDetailIntent.PopulatePropertyIntent> {
        return populatePropertyIntentPublisher
    }

    override fun render(state: PropertyDetailViewState) {
        state.property?.let { propertyWithPhotos ->
            if(!::property.isInitialized || property != propertyWithPhotos || property.photos != propertyWithPhotos.photos) {
                property = propertyWithPhotos
                properties.value?.let { properties ->
                    properties[properties.indexOf(
                        properties.single { property -> property.id == propertyId })] = propertyWithPhotos
                    displayDetail()
                }
            }
        }
    }

    private fun displayDetail() {
        properties.value?.let { properties ->
            property = properties.single { property -> property.id == propertyId }
        }

        with(binding) {
            val descriptionText = "\n${property.description}"
            description.setText(descriptionText)

            interestPointsChipGroup.removeAllViewsInLayout()
            property.interestPoints.forEach { interestPoint ->
                val newChip = layoutInflater.inflate(R.layout.layout_interest_point_chip_default,
                    binding.interestPointsChipGroup, false) as Chip
                newChip.text = resources.getString(interestPoint.place)
                newChip.isCheckable = false
                newChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                newChip.setTextColor(getColorStateList(requireContext(), R.color.chip_text_state_list))

                interestPointsChipGroup.addView(newChip)
            }

            entryDate.setText(Utils.formatDate(property.entryDate))

            if(property.status == PropertyStatus.SOLD) {
                layoutSoldDate.visibility = VISIBLE
                property.soldDate?.let { soldDate.setText(Utils.formatDate(it)) }
            } else {
                layoutSoldDate.visibility = GONE
            }
            status.setText(resources.getString(property.status.status))

            if(property.status == PropertyStatus.SOLD) {
                layoutSoldDate.visibility = VISIBLE
                property.soldDate?.let {
                    soldDate.setText(Utils.formatDate(it))
                }
            } else {
                layoutSoldDate.visibility = GONE
            }

            price.setText(property.price.toString())
            type.setText(property.type.type)
            surface.setText("${property.surface}")
            rooms.setText(property.rooms.toString())
            bathrooms.setText(property.bathRooms.toString())
            bedrooms.setText(property.bedRooms.toString())

            street.setText(property.address?.street)
            city.setText(property.address?.city)
            postalCode.setText(property.address?.postalCode)
            country.setText(property.address?.country)
            binding.state.setText(property.address?.state)
        }

        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment

            with(browseFragment.binding.toolBar) {
                title = property.titleInToolbar()

                if(!onBackPressedCallback.isEnabled) {
                    onBackPressedCallback.isEnabled = true
                }

                setNavigationOnClickListener {
                    if (resources.getBoolean(R.bool.isMasterDetail)) {
                        masterDetailOnBackPressed()
                    } else {
                        normalOnBackPressed()
                    }
                    onBackPressedCallback.isEnabled = false
                }
            }
        }

        PhotoDetailAdapter().apply {
            binding.photosRecyclerView.adapter = this
            setOnItemClickListener(this@PropertyDetailFragment)
            submitList(property.photos)
            notifyDataSetChanged()
        }

        if(binding.mapConstraintLayout.contentDescription != DETAIL_MAP_FINISH_LOADING) {
            activity?.runOnUiThread {
                (this.childFragmentManager.findFragmentById(R.id.map_detail_fragment) as SupportMapFragment)
                    .getMapAsync(this)
            }
        } else {
            moveCameraToPropertyInsideMap()
        }

        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.binding.toolBar.title = property.titleInToolbar()
        }
    }

    fun showDetails(propertyId: String?) {
        this.propertyId = propertyId!!

        properties.value?.let { properties ->
            if(properties.single { property -> property.id == propertyId }.photos.none { photo -> !photo.mainPhoto }) {
                populatePropertyIntentPublisher.onNext(PropertyDetailIntent.PopulatePropertyIntent(propertyId))
            }
            displayDetail()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        editItem = menu.findItem(R.id.navigation_edit)
        editItem.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_edit -> {
                val masterDetailFragment = this.parentFragment?.parentFragment as BrowseFragment

                if(masterDetailFragment.detail.childFragmentManager
                        .findFragmentByTag(R.id.navigation_edit.toString()) != null) {

                    val propertyUpdateFragment: PropertyUpdateFragment = masterDetailFragment.detail.childFragmentManager
                        .findFragmentByTag(R.id.navigation_edit.toString()) as PropertyUpdateFragment

                    val propertyId = property.id
                    propertyUpdateFragment.showDetails(propertyId)
                    val bundle = bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to propertyId
                    )
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_edit, bundle)
                } else {
                    val propertyId = property.id
                    val bundle = bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to propertyId
                    )
                    masterDetailFragment.detail.findNavController().navigate(R.id.navigation_edit, bundle)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            editItem.isVisible = false
            onBackPressedCallback.isEnabled = false
        } else {
            displayDetail()
            initializeToolbar()
            applyDisposition()
            onBackPressedCallback.isEnabled = true
            binding.detailFragment.fullScroll(ScrollView.FOCUS_UP)
            binding.photosRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        this.parentFragment?.let {
            val browseFragment = this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.setOnItemClickListener(this)
        }

        compositeDisposable.add(propertyDetailViewModel.states().subscribe(this::render))
        propertyDetailViewModel.processIntents(intents())

        showDetails(propertyId)
    }

    private fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (resources.getBoolean(R.bool.isMasterDetail)) {
                    masterDetailOnBackPressed()
                } else {
                    normalOnBackPressed()
                }
                isEnabled = false
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onItemClick(propertyId: String) {
        showDetails(propertyId = propertyId)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisposition()
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
            } else {}
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

        (binding.layoutPropertyAddress!!.layoutParams as ConstraintLayout.LayoutParams).apply {
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            endToStart = ConstraintLayout.LayoutParams.UNSET
            bottomToTop = binding.mapConstraintLayout.id
            horizontalWeight = 1f
            height = ConstraintLayout.LayoutParams.MATCH_PARENT
            leftMargin = 16
            rightMargin = 16
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

        (binding.mapConstraintLayout.layoutParams as ConstraintLayout.LayoutParams).apply {

            val containerLayoutParams = binding.container.layoutParams as FrameLayout.LayoutParams

            leftMargin = 16
            rightMargin = 16
            topMargin = 16
            bottomMargin = 16

            width = screenWidth(requireActivity())
            width -= containerLayoutParams.leftMargin + leftMargin
            width -= containerLayoutParams.rightMargin + rightMargin
            height = width

            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            startToEnd = ConstraintLayout.LayoutParams.UNSET
            topToBottom = binding.layoutPropertyAddress!!.id
            topToTop = ConstraintLayout.LayoutParams.UNSET
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            horizontalWeight = 1f
        }.also { layoutParams ->
            binding.mapConstraintLayout.layoutParams = layoutParams
        }
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

        (binding.layoutPropertyAddress!!.layoutParams as ConstraintLayout.LayoutParams).apply {
            endToEnd = ConstraintLayout.LayoutParams.UNSET
            endToStart = binding.mapConstraintLayout.id
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

        (binding.mapConstraintLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
            width = 0
            height = 0
            startToStart = ConstraintLayout.LayoutParams.UNSET
            startToEnd = binding.layoutPropertyAddress!!.id
            topToBottom = ConstraintLayout.LayoutParams.UNSET
            topToTop = binding.layoutPropertyAddress!!.id
            bottomToBottom = binding.layoutPropertyAddress!!.id
            horizontalWeight = 0.5f
            setMargins( 8, 16, 16, 16)
        }.also { layoutParams ->
            binding.mapConstraintLayout.layoutParams = layoutParams
        }
    }

    override fun initializeToolbar() {}

    private fun masterDetailOnBackPressed() {
        when(arguments?.getString(FROM)) {
            MapFragment::class.java.name -> {
                (parentFragment?.parentFragment as BrowseFragment)
                    .detail
                    .navController
                    .navigate(R.id.navigation_map)
            }
        }
    }

    private fun normalOnBackPressed() {
        when(arguments?.getString(FROM)) {
            ListFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseFragment).apply {
                    master.requireView().visibility = VISIBLE
                    detail.requireView().visibility = GONE
                    detail.navController.navigate(R.id.navigation_map)
                    binding.buttonContainer.visibility = VISIBLE
                    binding.listViewButton.isSelected = true
                    binding.mapViewButton.isSelected = false
                    (master.binding.recyclerView.adapter as ListAdapter).notifyDataSetChanged()
                }
            }
            MapFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseFragment).apply {
                    detail.navController.navigate(R.id.navigation_map)
                    master.requireView().visibility = GONE
                    detail.requireView().visibility = VISIBLE
                    binding.buttonContainer.visibility = VISIBLE
                    binding.listViewButton.isSelected = false
                    binding.mapViewButton.isSelected = true
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        moveCameraToPropertyInsideMap()
        val options = GoogleMapOptions().liteMode(true)
        mMap.mapType = options.mapType
        binding.mapConstraintLayout.contentDescription = DETAIL_MAP_NOT_FINISH_LOADING
        mMap.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        binding.mapConstraintLayout.contentDescription = DETAIL_MAP_FINISH_LOADING
    }

    private fun moveCameraToPropertyInsideMap() {
        mMap.clear()
        mMap.addMarker(MarkerOptions()
            .position(LatLng(property.address!!.latitude,
                property.address!!.longitude)
            )
        )

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(property.address!!.latitude,
                property.address!!.longitude), (DEFAULT_ZOOM + 3))

        mMap.moveCamera(cameraUpdate)
    }

    companion object {
        const val DETAIL_MAP_NOT_FINISH_LOADING = "detail_map_not_finish_loading"
        const val DETAIL_MAP_FINISH_LOADING = "detail_map_finish_loading"
    }

    override fun clickOnPhotoAtPosition(photoId: String) {
        detailPhotoAlertDialog = PhotoDetailDialogFragment().also {
            it.photo = property.photos.singleOrNull { photo -> photo.id == photoId }
        }
        detailPhotoAlertDialog.show(childFragmentManager, PhotoDetailDialogFragment.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}