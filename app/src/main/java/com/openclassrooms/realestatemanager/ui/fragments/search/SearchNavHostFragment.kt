package com.openclassrooms.realestatemanager.ui.fragments.search

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.di.property.search.SearchScope
import javax.inject.Inject
import javax.inject.Named

@SearchScope
class SearchNavHostFragment : NavHostFragment() {

    @Inject
    @Named("SearchFragmentFactory")
    lateinit var searchFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).searchComponent()
            .inject(this)
        childFragmentManager.fragmentFactory = searchFragmentFactory
        super.onAttach(context)
    }
}