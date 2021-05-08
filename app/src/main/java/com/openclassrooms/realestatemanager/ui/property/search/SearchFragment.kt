package com.openclassrooms.realestatemanager.ui.property.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment

/**
 * Fragment to Search one or several real estates.
 */
class SearchFragment : BaseFragment(R.layout.fragment_search, null) {

    lateinit var mainActivity: MainActivity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mainActivity = activity as MainActivity
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun initializeToolbar() {
        mainActivity.binding.toolBar.visibility = VISIBLE
        mainActivity.setSupportActionBar(mainActivity.binding.toolBar)

        mainActivity.binding.toolBar.setupWithNavController(
                mainActivity.navController,
                mainActivity.appBarConfiguration)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(hidden) {
            mainActivity.binding.toolBar.visibility = GONE
        } else {
            initializeToolbar()
        }
    }
}