package com.openclassrooms.realestatemanager.di.property.search

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.ui.fragments.search.SearchFragmentFactory
import com.openclassrooms.realestatemanager.ui.fragments.search.result.ResultSearchDetailFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object SearchFragmentModule {

    @JvmStatic
    @SearchScope
    @Named("SearchFragmentFactory")
    @Provides
    fun provideSearchFragmentFactory(): FragmentFactory {
        return SearchFragmentFactory()
    }

    @JvmStatic
    @SearchScope
    @Named("ResultSearchMasterDetailFragmentFactory")
    @Provides
    fun provideResultSearchMasterDetailFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
    ): FragmentFactory {
        return ResultSearchDetailFragmentFactory(viewModelFactory = viewModelFactory, registry = null)
    }
}
