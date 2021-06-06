package com.openclassrooms.realestatemanager.di.property.browse

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.fragments.property.browse.detail.BrowseDetailFragmentFactory
import com.openclassrooms.realestatemanager.util.GlideManager
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object BrowsePropertyFragmentsModule {

    @JvmStatic
    @BrowseScope
    @Named("BrowseDetailFragmentFactory")
    @Provides
    fun provideMasterDetailFragmentFactory(
            viewModelFactory: ViewModelProvider.Factory,
            glideManager: GlideManager,
    ): FragmentFactory {
        return BrowseDetailFragmentFactory(viewModelFactory = viewModelFactory,
                requestManager = glideManager, registry = null
        )
    }
}