package com.openclassrooms.realestatemanager.ui.property.search.result.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment.Companion.searchedProperties
import com.openclassrooms.realestatemanager.ui.property.shared.list.BaseListFragment

/**
 * Fragment to list real estates.
 */
class SearchListFragment : BaseListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        searchedProperties.observe(viewLifecycleOwner) { properties ->

            if(properties.isNotEmpty()) {
                setUpScreenForSuccess(properties)
            } else {
                setUpScreenForSuccess(emptyList())
                binding.propertiesRecyclerView.adapter!!.notifyDataSetChanged()
            }
        }
        binding.root.context.setTheme(R.style.AppTheme_Tertiary)
        return binding.root
    }
}