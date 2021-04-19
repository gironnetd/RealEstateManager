package com.openclassrooms.realestatemanager.ui.property.browse.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentDetailBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseMasterFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

/**
 * Fragment to display and edit a real estate.
 */
class DetailFragment@Inject
constructor(
        viewModelFactory: ViewModelProvider.Factory,
        val requestManager: GlideManager,
) : BaseFragment(R.layout.fragment_detail, viewModelFactory) {

    private var _binding: FragmentDetailBinding? = null
    val binding get() = _binding!!

    lateinit var property: Property

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        property = properties.single { property -> property.id == arguments?.getString("propertyId") }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        when(this.parentFragment?.parentFragment?.javaClass?.name) {
            BrowseMasterFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment).binding.buttonContainer.visibility = GONE

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

    private fun backPressedWhenTabletMode() {
        when(arguments?.getString("from")) {
            MapFragment::class.java.name -> {
                (parentFragment?.parentFragment as BrowseMasterDetailFragment)
                        .detail
                        .navController
                        .navigate(R.id.navigation_map)
            }
        }
    }

    private fun backPressedWhenNormalMode() {
        when(arguments?.getString("from")) {
            ListFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment)
                        .master
                        .navController
                        .navigate(R.id.navigation_list)
            }
            MapFragment::class.java.name -> {
                (this.parentFragment?.parentFragment as BrowseMasterFragment)
                        .master
                        .navController
                        .navigate(R.id.navigation_map)
            }
        }
    }
}