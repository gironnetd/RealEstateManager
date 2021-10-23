package com.openclassrooms.realestatemanager.ui.fragments.browsedetail

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import javax.inject.Inject
import javax.inject.Named

@BrowseScope
class BrowseDetailNavHostFragment : NavHostFragment() {

    @Inject
    @Named("BrowseDetailFragmentFactory")
    lateinit var browseDetailFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).browseComponent()
            .inject(this)
        childFragmentManager.fragmentFactory = browseDetailFragmentFactory
        super.onAttach(context)
    }
}
