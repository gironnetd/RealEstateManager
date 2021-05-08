package com.openclassrooms.realestatemanager.di.property

import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowsePropertyFragmentsModule
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.fragments.property.browse.detail.BrowseDetailNavHostFragment
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
import com.openclassrooms.realestatemanager.ui.property.browse.list.ListFragment
import dagger.Subcomponent

@BrowseScope
@Subcomponent(modules = [
    TestBrowseModule::class,
    BrowsePropertyFragmentsModule::class,
    TestPropertyViewModelModule::class
])
interface TestBrowseComponent : BrowseComponent {

    val apiService: FakePropertyApiService
    val propertyRepository: FakePropertyRepository

    @Subcomponent.Factory
    interface Factory {

        fun create(): TestBrowseComponent
    }

    override fun inject(listFragment: ListFragment)

    override fun inject(browseDetailNavHostFragment: BrowseDetailNavHostFragment)
}