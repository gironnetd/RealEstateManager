package com.openclassrooms.realestatemanager.fragments.realestate.display.master

import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.di.realestate.display.RealEstateScope
import com.openclassrooms.realestatemanager.view.realestate.display.detail.DetailFragment
import com.openclassrooms.realestatemanager.view.realestate.display.list.ListFragment
import com.openclassrooms.realestatemanager.view.realestate.display.map.MapFragment

@RealEstateScope
class MasterFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =
            when (className) {
                ListFragment::class.java.name -> {
                    val fragment = ListFragment()
                    fragment
                }

                DetailFragment::class.java.name -> {
                    val fragment = DetailFragment()
                    fragment
                }

                MapFragment::class.java.name -> {
                    val fragment = MapFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}
