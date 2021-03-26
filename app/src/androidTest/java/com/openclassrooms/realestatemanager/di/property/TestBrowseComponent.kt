package com.openclassrooms.realestatemanager.di.property

import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.di.property.browse.BrowseComponent
import com.openclassrooms.realestatemanager.di.property.browse.BrowsePropertyFragmentsModule
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.fragments.property.browse.master.MasterNavHostFragment
import com.openclassrooms.realestatemanager.fragments.property.browse.masterdetail.MasterDetailNavHostFragment
import com.openclassrooms.realestatemanager.repository.property.FakePropertyRepository
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

    override fun inject(masterNavHostFragment: MasterNavHostFragment)

    override fun inject(masterDetailNavHostFragment: MasterDetailNavHostFragment)
}