package com.openclassrooms.realestatemanager.ui.property.search

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentSearchBinding
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment

/**
 * Fragment to  Search one or several real estates.
 */
class PropertySearchFragment : BaseFragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    val binding get() = _binding!!

    private lateinit var innerInflater: LayoutInflater

    val mainActivity by lazy { activity as MainActivity  }

    val selectedTypes: MutableSet<PropertyType> by lazy { mutableSetOf() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val contextThemeWrapper: Context = ContextThemeWrapper(activity, R.style.AppTheme_Tertiary)
        innerInflater = inflater.cloneInContext(contextThemeWrapper)

        _binding = FragmentSearchBinding.inflate(innerInflater, container, false)

        super.onCreateView(innerInflater, container, savedInstanceState)
        return binding.root
    }

    fun initResultMenuItem() {}

    override fun initializeToolbar() {}
}