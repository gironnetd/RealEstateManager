package com.openclassrooms.realestatemanager.ui.fragments

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.openclassrooms.realestatemanager.ui.property.edit.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.search.MainSearchFragment
import com.openclassrooms.realestatemanager.ui.viewmodels.FakePropertiesViewModelFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMainFragmentFactory
@Inject constructor(
    private val viewModelFactory: FakePropertiesViewModelFactory,
    private val registry: ActivityResultRegistry?
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        when (className) {
            PropertyCreateFragment::class.java.name -> {
                val fragment = PropertyCreateFragment(viewModelFactory = viewModelFactory,
                    registry = registry)
                fragment
            }

            MainSearchFragment::class.java.name -> {
                val fragment = MainSearchFragment()
                fragment
            }
            else -> super.instantiate(classLoader, className)
        }
}