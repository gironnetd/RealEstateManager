package com.openclassrooms.realestatemanager.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.view.realestate.create.CreateFragment
import com.openclassrooms.realestatemanager.view.realestate.display.RealEstateFragment
import com.openclassrooms.realestatemanager.view.realestate.search.SearchFragment
import com.openclassrooms.realestatemanager.view.simulation.SimulationFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainFragmentFactory
@Inject constructor()
    : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

            when (className) {

                RealEstateFragment::class.java.name -> {
                    val fragment = RealEstateFragment()
                    fragment
                }

                SimulationFragment::class.java.name -> {
                    val fragment = SimulationFragment()
                    fragment
                }

                CreateFragment::class.java.name -> {
                    val fragment = CreateFragment()
                    fragment
                }

                SearchFragment::class.java.name -> {
                    val fragment = SearchFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}