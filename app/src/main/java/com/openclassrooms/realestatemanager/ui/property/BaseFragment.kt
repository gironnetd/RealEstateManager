package com.openclassrooms.realestatemanager.ui.property

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.models.Property

abstract class BaseFragment
constructor(
        @LayoutRes
        private val layoutRes: Int,
        private val viewModelFactory: ViewModelProvider.Factory?
): Fragment(layoutRes) {

        override fun onResume() {
                super.onResume()
                initializeToolbar()
        }

        abstract fun initializeToolbar()

        companion object {
                var properties: MutableList<Property> = mutableListOf()
        }
}