package com.openclassrooms.realestatemanager.ui.fragments.browsedetail

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.map.BrowseMapFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import javax.inject.Inject

@BrowseScope
class BrowseDetailFragmentFactory
@Inject constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val registry: ActivityResultRegistry?
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

            when (className) {
                BrowseMapFragment::class.java.name -> { BrowseMapFragment() }

                PropertyDetailFragment::class.java.name -> {
                    PropertyDetailFragment(viewModelFactory = viewModelFactory, registry = registry)
                }

                PropertyUpdateFragment::class.java.name -> {
                    PropertyUpdateFragment(viewModelFactory = viewModelFactory, registry = registry)
                }
                else -> super.instantiate(classLoader, className)
            }
}