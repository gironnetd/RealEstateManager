package com.openclassrooms.realestatemanager.ui.fragments.property.browse.detail

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

@BrowseScope
class BrowseDetailFragmentFactory
@Inject constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val requestManager: GlideManager,
        private val registry: ActivityResultRegistry?
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

            when (className) {
                MapFragment::class.java.name -> {
                    val fragment = MapFragment(requestManager = requestManager)
                    fragment
                }

                PropertyDetailFragment::class.java.name -> {
                    val fragment = PropertyDetailFragment(viewModelFactory = viewModelFactory,
                    )
                    fragment
                }

                PropertyUpdateFragment::class.java.name -> {
                    val fragment = PropertyUpdateFragment(viewModelFactory = viewModelFactory,
                        registry = registry
                    )
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}