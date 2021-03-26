package com.openclassrooms.realestatemanager.ui.property

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesViewModel

abstract class BasePropertyFragment
constructor(
        @LayoutRes
        private val layoutRes: Int,
        private val viewModelFactory: ViewModelProvider.Factory
): Fragment(layoutRes) {

        val propertiesViewModel: PropertiesViewModel by viewModels {
                viewModelFactory
        }

        companion object {
                var properties: MutableList<Property> = mutableListOf()
        }
}