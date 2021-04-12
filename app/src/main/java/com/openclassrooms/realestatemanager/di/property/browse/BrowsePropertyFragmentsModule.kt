package com.openclassrooms.realestatemanager.di.property.browse

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.fragments.property.browse.detail.MasterDetailFragmentFactory
import com.openclassrooms.realestatemanager.fragments.property.browse.master.MasterFragmentFactory
import com.openclassrooms.realestatemanager.util.GlideManager
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object BrowsePropertyFragmentsModule {

    @JvmStatic
    @BrowseScope
    @Named("MasterFragmentFactory")
    @Provides
    fun provideMasterFragmentFactory(
            viewModelFactory: ViewModelProvider.Factory,
            glideManager: GlideManager,
    ): FragmentFactory {
        return MasterFragmentFactory(viewModelFactory = viewModelFactory,
                requestManager = glideManager
        )
    }

    @JvmStatic
    @BrowseScope
    @Named("MasterDetailFragmentFactory")
    @Provides
    fun provideMasterDetailFragmentFactory(
            viewModelFactory: ViewModelProvider.Factory,
            glideManager: GlideManager,
    ): FragmentFactory {
        return MasterDetailFragmentFactory(viewModelFactory = viewModelFactory,
                requestManager = glideManager
        )
    }
}