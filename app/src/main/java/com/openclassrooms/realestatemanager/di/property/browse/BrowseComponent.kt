package com.openclassrooms.realestatemanager.di.property.browse

import com.openclassrooms.realestatemanager.ui.fragments.property.browse.detail.BrowseDetailNavHostFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [BrowsePropertyFragmentsModule::class])
interface BrowseComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): BrowseComponent
    }

    fun inject(listFragment: ListFragment)
    fun inject(browseDetailNavHostFragment: BrowseDetailNavHostFragment)
}