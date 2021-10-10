package com.openclassrooms.realestatemanager.ui.fragments.search.result

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.search.SearchScope
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.map.SearchMapFragment
import javax.inject.Inject

@SearchScope
class ResultSearchDetailFragmentFactory
@Inject constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val registry: ActivityResultRegistry?
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        when (className) {
            SearchMapFragment::class.java.name -> {
                SearchMapFragment()
            }

            PropertyDetailFragment::class.java.name -> {
                PropertyDetailFragment(viewModelFactory = viewModelFactory, registry = registry)
            }

            PropertyUpdateFragment::class.java.name -> {
                PropertyUpdateFragment(
                    viewModelFactory = viewModelFactory,
                    registry = registry
                )
            }
            else -> super.instantiate(classLoader, className)
        }
}