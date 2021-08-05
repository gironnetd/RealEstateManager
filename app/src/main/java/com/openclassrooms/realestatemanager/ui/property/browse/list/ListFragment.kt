package com.openclassrooms.realestatemanager.ui.property.browse.list

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat.START
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.*
import com.openclassrooms.realestatemanager.util.GlideManager
import java.util.*
import javax.inject.Inject

/**
 * Fragment to list real estates.
 */
class ListFragment
@Inject constructor() : BaseFragment(R.layout.fragment_list) {

    @Inject lateinit var requestManager: GlideManager

    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!

    private lateinit var recyclerAdapter: ListAdapter

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).browseComponent()
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        applyDisposition()
        initRecyclerView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        properties.observe(viewLifecycleOwner) { properties ->
            properties?.let {
                setUpScreenForSuccess(properties)
            } ?: setUpScreenForLoadingState()
        }
    }

    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment as BrowseFragment

            browseFragment.binding.toolBar.setNavigationOnClickListener {
                val mainActivity = activity as MainActivity
                if (!mainActivity.binding.drawerLayout.isDrawerOpen(START)) {
                    mainActivity.binding.drawerLayout.openDrawer(START)
                } else {
                    mainActivity.binding.drawerLayout.closeDrawer(START)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisposition()
    }

    private fun applyDisposition() {
        this.parentFragment?.let {

            if (resources.getBoolean(R.bool.isMasterDetail)) {
                screenWidth = screenWidth(requireActivity())

                binding.listFragment.layoutParams?.let { layoutParams ->
                    val masterWidthWeight = TypedValue()
                    resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)
                    layoutParams.width = (screenWidth * masterWidthWeight.float).toInt()
                }
                (binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams).topMargin = 0
            } else {
                binding.listFragment.layoutParams?.let { layoutParams ->
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                binding.recyclerView.layoutParams?.let { layoutParams ->
                    (layoutParams as ConstraintLayout.LayoutParams).topMargin =
                        resources.getDimension(R.dimen.list_properties_margin_top).toInt()
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListFragment.context)
            recyclerAdapter = ListAdapter(requestManager)
            adapter = recyclerAdapter
        }
    }

    private fun setUpScreenForSuccess(properties: List<Property>) {
        if (properties.isNotEmpty()) {
            binding.recyclerView.visibility = VISIBLE
            binding.noDataTextView.visibility = GONE

            if (!::recyclerAdapter.isInitialized) {
                initRecyclerView()
            }
            recyclerAdapter.apply {
                submitList(properties = properties)
                notifyDataSetChanged()
            }
        } else {
            binding.recyclerView.visibility = GONE
            binding.noDataTextView.visibility = VISIBLE
        }
    }

    private fun setUpScreenForLoadingState() {
        binding.recyclerView.visibility = GONE
        binding.noDataTextView.visibility = VISIBLE
    }
}