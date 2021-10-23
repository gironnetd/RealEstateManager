package com.openclassrooms.realestatemanager.ui.property.browse.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.openclassrooms.realestatemanager.ui.property.shared.list.BaseListFragment

/**
 * Fragment to list real estates.
 */
class BrowseListFragment : BaseListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        properties.observe(viewLifecycleOwner) { properties ->
            properties?.let {
                setUpScreenForSuccess(properties)
            } ?: setUpScreenForLoadingState()
        }
        return binding.root
    }
}
