package com.openclassrooms.realestatemanager.di.realestate

import com.openclassrooms.realestatemanager.di.realestate.display.RealEstateFragmentsModule
import com.openclassrooms.realestatemanager.di.realestate.display.RealEstateScope
import com.openclassrooms.realestatemanager.fragments.realestate.display.master.MasterNavHostFragment
import com.openclassrooms.realestatemanager.fragments.realestate.display.masterdetail.MasterDetailNavHostFragment
import dagger.Subcomponent

@RealEstateScope
@Subcomponent(modules = [
    RealEstateFragmentsModule::class
])
interface RealEstateComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): RealEstateComponent
    }

    fun inject(masterNavHostFragment: MasterNavHostFragment)

    fun inject(masterDetailNavHostFragment: MasterDetailNavHostFragment)
}