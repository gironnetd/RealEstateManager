package com.openclassrooms.realestatemanager.di.property

import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowseFragmentsModule
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.fragments.browsedetail.BrowseDetailNavHostFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [BrowseFragmentsModule::class])
interface TestBrowseComponent : BrowseComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TestBrowseComponent
    }

    override fun inject(browseDetailNavHostFragment: BrowseDetailNavHostFragment)
}