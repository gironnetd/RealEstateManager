package com.openclassrooms.realestatemanager.fragments.realestate.display.master

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import javax.inject.Inject
import javax.inject.Named

class MasterNavHostFragment : NavHostFragment() {

    @Inject
    @Named("MasterFragmentFactory")
    lateinit var masterFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).realEstateComponent()
                .inject(this)
        childFragmentManager.fragmentFactory = masterFragmentFactory
        super.onAttach(context)
    }
}