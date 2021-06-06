package com.openclassrooms.realestatemanager.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.ui.navigation.browse.MainFragmentNavigator

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        val navigator = MainFragmentNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)

        with(navController) {
            navigatorProvider.addNavigator(navigator)
            setGraph(R.navigation.navigation)
        }

        appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_search, R.id.navigation_create, R.id.navigation_real_estate)
                .setOpenableLayout(binding.drawerLayout)
                .build()

        with(binding) {
            toolBar.setupWithNavController(navController, appBarConfiguration)
            navigationView.setupWithNavController(navController)
            bottomNavigationView.setupWithNavController(navController)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) ||
                super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @VisibleForTesting
    fun setFragment(testFragment: Fragment) {
        for (fragment in navHostFragment.childFragmentManager.fragments) {
            navHostFragment.childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        val transaction = navHostFragment.childFragmentManager.beginTransaction()
        transaction.add(R.id.nav_host_fragment, testFragment)
        transaction.commit()
    }
}