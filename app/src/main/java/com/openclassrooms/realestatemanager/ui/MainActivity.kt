package com.openclassrooms.realestatemanager.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.navigation.MainFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewModel
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_CREATED
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.util.AppNotificationManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MviView<PropertiesIntent, PropertiesViewState> {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var navHostFragment: NavHostFragment

    private val loadPropertiesIntentPublisher =
        PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val propertiesViewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

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

    override fun onStart() {
        super.onStart()
        (application as BaseApplication).appComponent.inject(this)
        compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
        propertiesViewModel.processIntents(intents())
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

    override fun intents(): Observable<PropertiesIntent> {
        return Observable.merge(initialIntent(), loadPropertiesIntentPublisher())
    }

    private fun initialIntent(): Observable<PropertiesIntent.InitialIntent> {
        return Observable.just(PropertiesIntent.InitialIntent)
    }

    private fun loadPropertiesIntentPublisher(): Observable<PropertiesIntent.LoadPropertiesIntent> {
        return loadPropertiesIntentPublisher
    }

    override fun render(state: PropertiesViewState) {
        state.properties?.let { properties ->
            if(properties != BaseFragment.properties.value) {
                BaseFragment.properties.value = state.properties.toMutableList()
            }
        }

        state.uiNotification?.let { uiNotification ->
            if(uiNotification == PROPERTIES_FULLY_UPDATED) {
                showMessage(resources.getString(R.string.property_update_totally))
            }

            if(uiNotification == PROPERTIES_FULLY_CREATED) {
                val mNotificationManager = AppNotificationManager(this)
                mNotificationManager.showNotification(null, resources.getString(R.string.property_create_totally))
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}