package com.openclassrooms.realestatemanager.ui.fragments.search.result

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.di.property.search.SearchScope
import javax.inject.Inject
import javax.inject.Named

@SearchScope
class ResultSearchDetailNavHostFragment : NavHostFragment() {

    @Inject
    @Named("ResultSearchMasterDetailFragmentFactory")
    lateinit var resultSearchFragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).searchComponent()
            .inject(this)
        childFragmentManager.fragmentFactory = resultSearchFragmentFactory
        super.onAttach(context)
    }
}