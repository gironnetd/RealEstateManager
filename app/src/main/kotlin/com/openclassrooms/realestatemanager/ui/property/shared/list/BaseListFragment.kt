package com.openclassrooms.realestatemanager.ui.property.shared.list

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment

open class BaseListFragment : BaseFragment(R.layout.fragment_list) {

    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!

    private lateinit var recyclerAdapter: ListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        applyDisposition()
        initRecyclerView()

        defaultCurrency.observe(viewLifecycleOwner) {
            properties.value?.let { properties ->
                recyclerAdapter.submitList(properties)
            }
        }

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisposition()
    }

    private fun applyDisposition() {
        this.parentFragment?.let {
            if (resources.getBoolean(R.bool.isMasterDetail)) {
                screenWidth = screenWidth(requireActivity())

                (binding.propertiesRecyclerView.layoutParams as FrameLayout.LayoutParams).let { layoutParams ->
                    layoutParams.topMargin = 0
                    val masterWidthWeight = TypedValue()
                    resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)
                    layoutParams.width = (screenWidth * masterWidthWeight.float).toInt()
                }
            } else {
                binding.propertiesRecyclerView.layoutParams?.let { layoutParams ->
                    (layoutParams as FrameLayout.LayoutParams).width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.topMargin =
                        resources.getDimension(R.dimen.list_properties_margin_top).toInt()
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.propertiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseListFragment.requireContext())
            recyclerAdapter = ListAdapter()
            adapter = recyclerAdapter
        }
    }

    fun setUpScreenForSuccess(properties: List<Property>) {
        if (properties.isNotEmpty()) {
            binding.propertiesRecyclerView.visibility = View.VISIBLE

            if (!::recyclerAdapter.isInitialized) { initRecyclerView() }
            recyclerAdapter.submitList(properties)
        } else {
            binding.propertiesRecyclerView.visibility = View.GONE
        }
    }

    fun setUpScreenForLoadingState() {
        binding.propertiesRecyclerView.visibility = View.GONE
    }
}
