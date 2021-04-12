package com.openclassrooms.realestatemanager.di.property.browse

import com.openclassrooms.realestatemanager.di.property.PropertyViewModelModule
import com.openclassrooms.realestatemanager.fragments.property.browse.detail.MasterDetailNavHostFragment
import com.openclassrooms.realestatemanager.fragments.property.browse.master.MasterNavHostFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [
    BrowseModule::class,
    BrowsePropertyFragmentsModule::class,
    PropertyViewModelModule::class
])
interface BrowseComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): BrowseComponent
    }

    fun inject(masterNavHostFragment: MasterNavHostFragment)

    fun inject(masterDetailNavHostFragment: MasterDetailNavHostFragment)
}