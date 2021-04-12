package com.openclassrooms.realestatemanager.ui.property.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment

/**
 * Fragment to Create a real estate.
 */
class PropertyCreateFragment : BasePropertyFragment(R.layout.fragment_create, null) {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun initializeToolbar() {
        val mainActivity = activity as MainActivity
        mainActivity.binding.toolBar.visibility = View.VISIBLE
        mainActivity.setSupportActionBar(mainActivity.binding.toolBar)

        mainActivity.binding.toolBar.setupWithNavController(
                mainActivity.navController,
                mainActivity.appBarConfiguration)
    }
}