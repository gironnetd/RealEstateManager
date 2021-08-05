package com.openclassrooms.realestatemanager.di.property

import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowsePropertyFragmentsModule
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.ui.fragments.property.browse.detail.BrowseDetailNavHostFragment
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [BrowsePropertyFragmentsModule::class])
interface TestBrowseComponent : BrowseComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TestBrowseComponent
    }

    override fun inject(listFragment: ListFragment)
    override fun inject(browseDetailNavHostFragment: BrowseDetailNavHostFragment)
}