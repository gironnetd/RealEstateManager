package com.openclassrooms.realestatemanager.ui.property.browse.edit

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.util.Constants.FROM
import com.openclassrooms.realestatemanager.util.Constants.PROPERTY_ID

/**
 * Fragment to edit and update a real estate.
 */
class EditFragment : BaseFragment(R.layout.fragment_edit, null) {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    lateinit var property: Property

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        property = properties.single { property -> property.id == arguments?.getString(PROPERTY_ID) }
        setHasOptionsMenu(true)
        configureView()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val editItem = menu.findItem(R.id.navigation_edit)
        editItem.isVisible = false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureView()
    }

    private fun configureView() {
        val detailLayoutParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val detailFragment = this.parentFragment as NavHostFragment

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        if(!resources.getBoolean(R.bool.isMasterDetail)) {
            detailLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            detailLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            detailLayoutParams.leftMargin = 0
            detailFragment.requireView().layoutParams = detailLayoutParams
            detailFragment.requireView().requestLayout()
        }
    }

    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val browseFragment =  this.parentFragment?.parentFragment as BrowseFragment
            browseFragment.binding.toolBar.setNavigationOnClickListener {
                if(resources.getBoolean(R.bool.isMasterDetail)) {
                    backPressedWhenTabletMode()
                } else {
                    backPressedWhenNormalMode()
                }
            }
        }
    }

    private fun backPressedWhenTabletMode() {
        (parentFragment?.parentFragment as BrowseFragment)
                .detail
                .navController
                .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to property.id))
    }

    private fun backPressedWhenNormalMode() {
        (this.parentFragment?.parentFragment as BrowseFragment)
                .detail
                .navController
                .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to property.id))
    }
}

