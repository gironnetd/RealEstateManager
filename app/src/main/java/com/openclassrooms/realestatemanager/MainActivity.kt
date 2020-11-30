package com.openclassrooms.realestatemanager

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
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