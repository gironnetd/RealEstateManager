package com.openclassrooms.realestatemanager.ui.fragments.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.di.property.search.SearchScope
import com.openclassrooms.realestatemanager.ui.property.search.PropertySearchFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import javax.inject.Inject

@SearchScope
class SearchFragmentFactory @Inject constructor() : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        when (className) {
            PropertySearchFragment::class.java.name -> {
                PropertySearchFragment()
            }

            BrowseResultFragment::class.java.name -> {
                BrowseResultFragment()
            }
            else -> super.instantiate(classLoader, className)
        }
}