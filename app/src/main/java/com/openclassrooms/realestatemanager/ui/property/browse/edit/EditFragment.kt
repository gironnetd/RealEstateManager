package com.openclassrooms.realestatemanager.ui.property.browse.edit

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentEditBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
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
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val editItem = menu.findItem(R.id.navigation_edit)
        editItem.isVisible = false
    }

    override fun initializeToolbar() {
        when(this.parentFragment?.parentFragment?.javaClass?.name) {

            BrowseMasterFragment::class.java.name -> {
                val masterFragment = this.parentFragment?.parentFragment as BrowseMasterFragment
                masterFragment.binding.toolBar.setNavigationOnClickListener {
                    backPressedWhenNormalMode()
                }
            }

            BrowseMasterDetailFragment::class.java.name -> {
                val masterDetailFragment =  this.parentFragment?.parentFragment as BrowseMasterDetailFragment

                masterDetailFragment.binding.toolBar.setNavigationOnClickListener {
                    backPressedWhenTabletMode()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when(this.parentFragment?.parentFragment?.javaClass?.name) {

            BrowseMasterFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment).binding.buttonContainer.visibility = View.GONE

                activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPressedWhenNormalMode()
                    }
                })
            }

            BrowseMasterDetailFragment::class.java.name -> {
                activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPressedWhenTabletMode()
                    }
                })
            }
        }
    }

    private fun backPressedWhenTabletMode() {
        (parentFragment?.parentFragment as BrowseMasterDetailFragment)
                .detail
                .navController
                .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to property.id))
    }

    private fun backPressedWhenNormalMode() {
        (this.parentFragment?.parentFragment as BrowseMasterFragment)
                .master
                .navController
                .navigate(R.id.navigation_detail, bundleOf(FROM to arguments?.getString(FROM),
                        PROPERTY_ID to property.id))
    }
}

