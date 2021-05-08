package com.openclassrooms.realestatemanager.fragments.property.browse.detail

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.edit.EditFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

@BrowseScope
class BrowseDetailFragmentFactory
@Inject
constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val requestManager: GlideManager,
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

            when (className) {
                MapFragment::class.java.name -> {
                    val fragment = MapFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager)
                    fragment
                }

                DetailFragment::class.java.name -> {
                    val fragment = DetailFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager)
                    fragment
                }

                EditFragment::class.java.name -> {
                    val fragment = EditFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}