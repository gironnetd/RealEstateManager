package com.openclassrooms.realestatemanager.fragments.property.browse.masterdetail

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import javax.inject.Inject
import javax.inject.Named

@BrowseScope
class MasterDetailNavHostFragment : NavHostFragment() {

    @Inject
    @Named("MasterDetailFragmentFactory")
    lateinit var masterDetailFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).browseComponent()
                .inject(this)
        childFragmentManager.fragmentFactory = masterDetailFragmentFactory
        super.onAttach(context)
    }
}