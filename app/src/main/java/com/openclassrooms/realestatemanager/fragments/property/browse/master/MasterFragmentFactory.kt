package com.openclassrooms.realestatemanager.fragments.property.browse.master

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.detail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.PropertyMapFragment
import com.openclassrooms.realestatemanager.ui.property.browse.properties.PropertiesFragment
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

@BrowseScope
class MasterFragmentFactory
@Inject
constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val requestManager: GlideManager,
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =
            when (className) {
                PropertiesFragment::class.java.name -> {
                    val fragment = PropertiesFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager
                    )
                    fragment
                }

                PropertyDetailFragment::class.java.name -> {
                    val fragment = PropertyDetailFragment()
                    fragment
                }

                PropertyMapFragment::class.java.name -> {
                    val fragment = PropertyMapFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}
