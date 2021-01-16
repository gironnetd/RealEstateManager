package com.openclassrooms.realestatemanager.di.realestate.display

import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.fragments.realestate.display.master.MasterFragmentFactory
import com.openclassrooms.realestatemanager.fragments.realestate.display.masterdetail.MasterDetailFragmentFactory
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object RealEstateFragmentsModule {

    @JvmStatic
    @RealEstateScope
    @Provides
    @Named("MasterFragmentFactory")
    fun provideMasterFragmentFactory(
    ): FragmentFactory {
        return MasterFragmentFactory()
    }

    @JvmStatic
    @RealEstateScope
    @Provides
    @Named("MasterDetailFragmentFactory")
    fun provideMasterDetailFragmentFactory(
    ): FragmentFactory {
        return MasterDetailFragmentFactory()
    }
}