package com.openclassrooms.realestatemanager.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.ui.property.create.CreateFragment
import com.openclassrooms.realestatemanager.ui.property.search.SearchFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainFragmentFactory
@Inject
constructor() : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

            when (className) {
                CreateFragment::class.java.name -> {
                    val fragment = CreateFragment()
                    fragment
                }

                SearchFragment::class.java.name -> {
                    val fragment = SearchFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}