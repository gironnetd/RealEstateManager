package com.openclassrooms.realestatemanager.di.property.browse

import com.openclassrooms.realestatemanager.ui.fragments.browsedetail.BrowseDetailNavHostFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [BrowseFragmentsModule::class])
interface BrowseComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): BrowseComponent
    }

    fun inject(browseDetailNavHostFragment: BrowseDetailNavHostFragment)
}
