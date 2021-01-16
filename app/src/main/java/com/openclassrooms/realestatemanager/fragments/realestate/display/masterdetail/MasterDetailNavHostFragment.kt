package com.openclassrooms.realestatemanager.fragments.realestate.display.masterdetail

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import javax.inject.Inject
import javax.inject.Named

class MasterDetailNavHostFragment : NavHostFragment() {

    @Inject
    @Named("MasterDetailFragmentFactory")
    lateinit var masterDetailFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).realEstateComponent()
                .inject(this)
        childFragmentManager.fragmentFactory = masterDetailFragmentFactory
        super.onAttach(context)
    }
}