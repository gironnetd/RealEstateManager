package com.openclassrooms.realestatemanager.ui.fragments

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainNavHostFragment : NavHostFragment() {

    @Inject
    lateinit var mainFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).appComponent
            .inject(this)
        childFragmentManager.fragmentFactory = mainFragmentFactory
        super.onAttach(context)
    }
}
