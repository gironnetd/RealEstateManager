package com.openclassrooms.realestatemanager.ui.fragments

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.search.SearchFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainFragmentFactory
@Inject constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    // private val requestManager: GlideManager,
    private val registry: ActivityResultRegistry?
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =

            when (className) {
                PropertyCreateFragment::class.java.name -> {
                    val fragment = PropertyCreateFragment(viewModelFactory = viewModelFactory,
                        registry = registry)
                    fragment
                }

                SearchFragment::class.java.name -> {
                    val fragment = SearchFragment()
                    fragment
                }
                else -> super.instantiate(classLoader, className)
            }
}