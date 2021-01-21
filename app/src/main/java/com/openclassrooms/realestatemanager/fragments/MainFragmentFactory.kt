package com.openclassrooms.realestatemanager.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.search.PropertySearchFragment
import com.openclassrooms.realestatemanager.ui.simulation.SimulationFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainFragmentFactory
@Inject
constructor(
        //private val viewModelFactory: ViewModelProvider.Factory
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

            when (className) {
                BrowseFragment::class.java.name -> {
                    val fragment = BrowseFragment()
                    fragment
                }

//                BrowseFragment::class.java.name -> {
//                    val fragment = BrowseFragment(viewModelFactory = viewModelFactory)
//                    fragment
//                }

                SimulationFragment::class.java.name -> {
                    val fragment = SimulationFragment()
                    fragment
                }

                PropertyCreateFragment::class.java.name -> {
                    val fragment = PropertyCreateFragment()
                    fragment
                }

                PropertySearchFragment::class.java.name -> {
                    val fragment = PropertySearchFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}