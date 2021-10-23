package com.openclassrooms.realestatemanager.di.property.search

import com.openclassrooms.realestatemanager.ui.fragments.search.SearchNavHostFragment
import com.openclassrooms.realestatemanager.ui.fragments.search.result.ResultSearchDetailNavHostFragment
import dagger.Subcomponent

@SearchScope
@Subcomponent(modules = [SearchFragmentModule::class])
interface SearchComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SearchComponent
    }

    fun inject(searchNavHostFragment: SearchNavHostFragment)
    fun inject(resultSearchDetailNavHostFragment: ResultSearchDetailNavHostFragment)
}
