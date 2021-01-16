package com.openclassrooms.realestatemanager.fragments.realestate.display.masterdetail

import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.di.realestate.display.RealEstateScope
import com.openclassrooms.realestatemanager.view.realestate.display.detail.DetailFragment
import com.openclassrooms.realestatemanager.view.realestate.display.edit.EditFragment
import com.openclassrooms.realestatemanager.view.realestate.display.map.MapFragment

@RealEstateScope
class MasterDetailFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

            when (className) {
                MapFragment::class.java.name -> {
                    val fragment = MapFragment()
                    fragment
                }

                DetailFragment::class.java.name -> {
                    val fragment = DetailFragment()
                    fragment
                }

                EditFragment::class.java.name -> {
                    val fragment = EditFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}