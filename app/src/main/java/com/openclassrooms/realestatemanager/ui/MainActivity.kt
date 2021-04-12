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
import com.openclassrooms.realestatemanager.ui.navigation.KeepStateNavigator
import com.openclassrooms.realestatemanager.ui.property.create.PropertyCreateFragment
import com.openclassrooms.realestatemanager.ui.property.search.PropertySearchFragment
import com.openclassrooms.realestatemanager.ui.simulation.SimulationFragment

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

        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider.addNavigator(navigator)

        navController.setGraph(R.navigation.navigation)

        appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_simulation,
                R.id.navigation_search, R.id.navigation_create, R.id.navigation_real_estate)
                .setOpenableLayout(binding.drawerLayout)
                .build()

        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setupWithNavController(navController)

        initCreateFloatingActionButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val primaryNavigationFragment =
                (navHostFragment.childFragmentManager.primaryNavigationFragment as Fragment)::class.java.name

        outState.putString(PRIMARY_NAVIGATION_FRAGMENT, primaryNavigationFragment)
            for (fragment in supportFragmentManager.fragments) {
                    supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        when(savedInstanceState?.getString(PRIMARY_NAVIGATION_FRAGMENT)) {
            SimulationFragment::class.java.name -> {
                navController.navigate(R.id.navigation_simulation)
            }
            PropertyCreateFragment::class.java.name -> {
                navController.navigate(R.id.navigation_create)
            }
            PropertySearchFragment::class.java.name -> {
                navController.navigate(R.id.navigation_search)
            }
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

    private fun initCreateFloatingActionButton() {
        binding.createFloatingActionButton.setOnClickListener {
            navController.navigate(R.id.navigation_create)
        }
    }

    @VisibleForTesting
    fun setFragment(fragment: Fragment) {
        val transaction = navHostFragment.childFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }

    companion object {
        const val PRIMARY_NAVIGATION_FRAGMENT = "primary_navigation_fragment"
    }
}