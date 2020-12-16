package com.openclassrooms.realestatemanager.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_list,
                R.id.navigation_simulation, R.id.navigation_search, R.id.navigation_create,
                R.id.navigation_real_estate, R.id.navigation_master_detail_real_estate)
                .setOpenableLayout(binding.drawerLayout)
                .build()
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setupWithNavController(navController)

        if(navController.currentDestination?.id == R.id.navigation_list ||
                navController.currentDestination?.id ==
                R.id.navigation_master_detail_real_estate) {
            navController.navigate(R.id.navigation_real_estate)
        }

        initCreateFloatingActionButton()
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
}