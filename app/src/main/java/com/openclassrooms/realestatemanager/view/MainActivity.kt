package com.openclassrooms.realestatemanager.view

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.util.Utils

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
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph)
                .setOpenableLayout(binding.drawerLayout)
                .build()
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)

        configureTextViewMain()
        configureTextViewQuantity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun configureTextViewMain() {
        binding.activityMainActivityTextViewMain!!.textSize = 15f
        binding.activityMainActivityTextViewMain!!.text = "Le premier bien immobilier enregistré vaut "
    }

    private fun configureTextViewQuantity() {
        val quantity = Utils.convertDollarToEuro(100)
        binding.activityMainActivityTextViewQuantity!!.textSize = 20f
        binding.activityMainActivityTextViewQuantity!!.text = quantity.toString()
    }
}