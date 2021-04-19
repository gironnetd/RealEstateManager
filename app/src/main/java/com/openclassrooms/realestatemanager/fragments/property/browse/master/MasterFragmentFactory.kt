package com.openclassrooms.realestatemanager.fragments.property.browse.master

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.property.browse.detail.DetailFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import com.openclassrooms.realestatemanager.ui.property.browse.map.MapFragment
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
                ListFragment::class.java.name -> {
                    val fragment = ListFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager
                    )
                    fragment
                }

                DetailFragment::class.java.name -> {
                    val fragment = DetailFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager)
                    fragment
                }

                MapFragment::class.java.name -> {
                    val fragment = MapFragment(viewModelFactory = viewModelFactory,
                            requestManager = requestManager
                    )
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}
