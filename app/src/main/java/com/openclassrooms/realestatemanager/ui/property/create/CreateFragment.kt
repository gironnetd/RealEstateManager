package com.openclassrooms.realestatemanager.ui.property.create

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
 * Fragment to Create a real estate.
 */
class CreateFragment : BaseFragment(R.layout.fragment_create) {

    lateinit var mainActivity: MainActivity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mainActivity = activity as MainActivity
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(hidden) {
            mainActivity.binding.toolBar.visibility = GONE
        } else {
            initializeToolbar()
        }
    }

    override fun initializeToolbar() {
        with(mainActivity) {
            binding.toolBar.visibility = VISIBLE
            setSupportActionBar(binding.toolBar)
            binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        }
    }
}