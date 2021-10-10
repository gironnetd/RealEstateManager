package com.openclassrooms.realestatemanager.di.property.browse

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.ui.fragments.browsedetail.BrowseDetailFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object BrowseFragmentsModule {

    @JvmStatic
    @BrowseScope
    @Named("BrowseDetailFragmentFactory")
    @Provides
    fun provideMasterDetailFragmentFactory(
            viewModelFactory: ViewModelProvider.Factory,
    ): FragmentFactory {
        return BrowseDetailFragmentFactory(viewModelFactory = viewModelFactory, registry = null)
    }
}