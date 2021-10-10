package com.openclassrooms.realestatemanager.ui

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.toSpannable
import androidx.core.view.GravityCompat.START
import androidx.core.view.MenuItemCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.ripple.RippleUtils
import com.google.android.material.snackbar.Snackbar
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.BuildConfig
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivityMainBinding
import com.openclassrooms.realestatemanager.ui.mvibase.MviView
import com.openclassrooms.realestatemanager.ui.navigation.MainFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewModel
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_CREATED
import com.openclassrooms.realestatemanager.ui.property.properties.PropertiesViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.setting.Currency.DOLLARS
import com.openclassrooms.realestatemanager.ui.property.setting.Currency.EUROS
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.AppNotificationManager
import com.openclassrooms.realestatemanager.util.Constants.DEFAULT_CURRENCY
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MviView<PropertiesIntent, PropertiesViewState> {

    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var navHostFragment: NavHostFragment

    private val loadPropertiesIntentPublisher = PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sharedPreferences: SharedPreferences

    val placesClient: PlacesClient by lazy {
        if(!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(this)
    }

    private val propertiesViewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as BaseApplication).appComponent.inject(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.inflateMenu(R.menu.menu_action_bar)
        setSupportActionBar(binding.toolBar)

        initDefaultCurrency()

        navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        val navigator = MainFragmentNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)

        with(navController) {
            navigatorProvider.addNavigator(navigator)
            setGraph(R.navigation.navigation)
        }

        appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_browse, R.id.navigation_create, R.id.navigation_main_search)
                .setOpenableLayout(binding.drawerLayout)
                .build()

        with(binding) {
            toolBar.setupWithNavController(navController, appBarConfiguration)
            navigationView.setupWithNavController(navController)
            navigationView.itemIconTintList = null
            bottomNavigationView.setupWithNavController(navController)
            bottomNavigationView.itemIconTintList = null

            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
                var colors: IntArray = intArrayOf()

                (bottomNavigationView.getChildAt(0) as BottomNavigationMenuView)
                        .children.iterator().forEach { bottomNavigationItemView ->
                            when (bottomNavigationItemView.id) {
                                R.id.navigation_browse -> {
                                    colors = intArrayOf(ResourcesCompat.getColor(resources, R.color.colorPrimaryRipple, null))
                                }
                                R.id.navigation_create -> {
                                    colors = intArrayOf(ResourcesCompat.getColor(resources, R.color.colorSecondaryRipple, null))
                                }
                                R.id.navigation_main_search -> {
                                    colors = intArrayOf(ResourcesCompat.getColor(resources, R.color.colorTertiaryRipple, null))
                                }
                            }
                            (bottomNavigationItemView as BottomNavigationItemView)
                                    .setItemBackground(
                                            RippleDrawable(
                                                    RippleUtils.convertToRippleDrawableColor(ColorStateList(states, colors)),
                                                    null,
                                                    null
                                            )
                                    )
                        }
            }

            navigationView.menu.children.iterator().forEach { menuItem ->
                when(menuItem.itemId) {
                    R.id.navigation_browse -> {

                        menuItem.setIcon(R.drawable.ic_baseline_home_work_24)
                        SpannableString(menuItem.title.toSpannable()).apply {
                            setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            menuItem.title = this
                        }

                        navigationView.itemBackground = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.navigation_view_all_properties_menu_item_background_color_state,
                                null
                        )

                        menuItem.setOnMenuItemClickListener {
                            menuItem.setIcon(R.drawable.ic_baseline_home_work_24)
                            SpannableString(menuItem.title.toSpannable()).apply {
                                setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                menuItem.title = this
                            }
                            navigationView.itemBackground = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.navigation_view_all_properties_menu_item_background_color_state,
                                    null
                            )
                            binding.drawerLayout.closeDrawer(START)
                            navController.navigate(R.id.navigation_browse)
                            true
                        }
                    }
                    R.id.navigation_create -> {

                        menuItem.setOnMenuItemClickListener {
                            menuItem.setIcon(R.drawable.ic_baseline_add_business_24)
                            SpannableString(menuItem.title.toSpannable()).apply {
                                setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorSecondary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                menuItem.title = this
                            }

                            navigationView.itemBackground = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.navigation_view_create_property_menu_item_background_color_state,
                                    null
                            )
                            binding.drawerLayout.closeDrawer(START)
                            navController.navigate(R.id.navigation_create)
                            true
                        }
                    }

                    R.id.navigation_main_search -> {
                        menuItem.setOnMenuItemClickListener {
                            MenuItemCompat.setIconTintList(menuItem, ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)))
                            menuItem.setIcon(R.drawable.ic_baseline_search_selected_24)
                            SpannableString(menuItem.title.toSpannable()).apply {
                                setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                menuItem.title = this
                            }
                            navigationView.itemBackground = ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.navigation_view_search_properties_menu_item_background_color_state,
                                    null
                            )
                            binding.drawerLayout.closeDrawer(START)
                            navController.navigate(R.id.navigation_main_search)
                            true
                        }
                    }
                    R.id.euros_choice -> {
                        navigationView.itemBackground = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.navigation_view_choose_default_currency_menu_item_background_color_state,
                                null
                        )
                    }
                    R.id.dollars_choice -> {
                        navigationView.itemBackground = ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.navigation_view_choose_default_currency_menu_item_background_color_state,
                                null
                        )
                    }
                }
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.euros_choice -> {
                        (menuItem.actionView as MaterialRadioButton).isChecked = true
                        sharedPreferences.edit().putString(DEFAULT_CURRENCY, EUROS.currency).apply().let {
                            BaseFragment.defaultCurrency.value = EUROS.currency
                        }
                        (navigationView.menu.findItem(R.id.dollars_choice).actionView as MaterialRadioButton).isChecked = false
                        binding.drawerLayout.closeDrawer(START)
                    }
                    R.id.dollars_choice -> {
                        (menuItem.actionView as MaterialRadioButton).isChecked = true
                        sharedPreferences.edit().putString(DEFAULT_CURRENCY, DOLLARS.currency).apply().let {
                            BaseFragment.defaultCurrency.value = DOLLARS.currency
                        }
                        (navigationView.menu.findItem(R.id.euros_choice).actionView as MaterialRadioButton).isChecked = false
                        binding.drawerLayout.closeDrawer(START)
                    }
                }
                true
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->

                navigationView.menu.children.iterator().forEach { menuItem ->
                    if(menuItem.itemId == destination.id) {
                        when(menuItem.itemId) {
                            R.id.navigation_browse -> {
                                navigationView.itemBackground = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.navigation_view_all_properties_menu_item_background_color_state,
                                        null
                                )
                                menuItem.setIcon(R.drawable.ic_baseline_home_work_24)
                                SpannableString(menuItem.title.toSpannable()).apply {
                                    setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    menuItem.title = this
                                }
                            }
                            R.id.navigation_create -> {
                                navigationView.itemBackground = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.navigation_view_create_property_menu_item_background_color_state,
                                        null
                                )
                                menuItem.setIcon(R.drawable.ic_baseline_add_business_24)
                                SpannableString(menuItem.title.toSpannable()).apply {
                                    setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorSecondary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    menuItem.title = this
                                }
                            }
                            R.id.navigation_main_search -> {
                                navigationView.itemBackground = ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.navigation_view_search_properties_menu_item_background_color_state,
                                        null
                                )
                                MenuItemCompat.setIconTintList(menuItem, ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)))
                                menuItem.setIcon(R.drawable.ic_baseline_search_selected_24)
                                SpannableString(menuItem.title.toSpannable()).apply {
                                    setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    menuItem.title = this
                                }
                            }
                        }
                    } else {
                        when(menuItem.itemId) {
                            R.id.navigation_browse -> {
                                menuItem.setIcon(R.drawable.ic_outline_home_work_24)
                            }
                            R.id.navigation_create -> {
                                menuItem.setIcon(R.drawable.ic_outline_add_business_24)
                            }
                            R.id.navigation_main_search -> {
                                MenuItemCompat.setIconTintList(menuItem, null)
                                menuItem.setIcon(R.drawable.ic_baseline_search_not_selected_24)
                            }
                        }

                        SpannableString(menuItem.title.toSpannable()).apply {
                            for (span in this.getSpans(0, this.length, Any::class.java)) {
                                if (span is CharacterStyle) removeSpan(span)
                            }
                            menuItem.title = this
                        }
                    }
                }

                bottomNavigationView.menu.children.iterator().forEach { menuItem ->
                    if(menuItem.itemId == destination.id) {
                        when(menuItem.itemId) {
                            R.id.navigation_browse -> {
                                menuItem.setIcon(R.drawable.ic_baseline_home_work_24)
                                if(bottomNavigationView.labelVisibilityMode == LABEL_VISIBILITY_LABELED) {
                                    SpannableString(menuItem.title.toSpannable()).apply {
                                        setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        menuItem.title = this
                                    }
                                }
                            }
                            R.id.navigation_create -> {
                                menuItem.setIcon(R.drawable.ic_baseline_add_business_24)
                                if(bottomNavigationView.labelVisibilityMode == LABEL_VISIBILITY_LABELED) {
                                    SpannableString(menuItem.title.toSpannable()).apply {
                                        setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorSecondary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        menuItem.title = this
                                    }
                                }
                            }
                            R.id.navigation_main_search -> {
                                MenuItemCompat.setIconTintList(menuItem, ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)))
                                menuItem.setIcon(R.drawable.ic_baseline_search_selected_24)
                                if(bottomNavigationView.labelVisibilityMode == LABEL_VISIBILITY_LABELED) {
                                    SpannableString(menuItem.title.toSpannable()).apply {
                                        setSpan(ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorTertiary, null)), 0, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                        menuItem.title = this
                                    }
                                }
                            }
                        }
                    } else {
                        when(menuItem.itemId) {
                            R.id.navigation_browse -> {
                                menuItem.setIcon(R.drawable.ic_outline_home_work_24)
                            }
                            R.id.navigation_create -> {
                                menuItem.setIcon(R.drawable.ic_outline_add_business_24)
                            }
                            R.id.navigation_main_search -> {
                                MenuItemCompat.setIconTintList(menuItem, null)
                                menuItem.setIcon(R.drawable.ic_baseline_search_not_selected_24)
                            }
                        }

                        SpannableString(menuItem.title.toSpannable()).apply {
                            for (span in this.getSpans(0, this.length, Any::class.java)) {
                                if (span is CharacterStyle) removeSpan(span)
                            }
                            menuItem.title = this
                        }
                    }
                }
            }
        }
    }

    private fun initDefaultCurrency() {
        val defaultCurrency = sharedPreferences.getString(DEFAULT_CURRENCY, EUROS.currency)

        with(binding) {
            when(defaultCurrency) {
                EUROS.currency -> {
                    (navigationView.menu.findItem(R.id.euros_choice).actionView as MaterialRadioButton).isChecked = true
                    BaseFragment.defaultCurrency.value = EUROS.currency
                }
                DOLLARS.currency -> {
                    (navigationView.menu.findItem(R.id.dollars_choice).actionView as MaterialRadioButton).isChecked = true
                    BaseFragment.defaultCurrency.value = DOLLARS.currency
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
        propertiesViewModel.processIntents(intents())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)

        menu?.let {
            val searchItem = it.findItem(R.id.navigation_main_search)
            searchItem.setOnMenuItemClickListener {
                navController.navigate(R.id.navigation_main_search)
                true
            }
        }
        return true
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
        if(state.inProgress != null) {
            if(state.inProgress) {
                BaseFragment.properties.value?.let { properties ->
                    if(properties.isNotEmpty()) {
                        binding.loadingProperties.visibility = GONE
                    }
                }
            }
        } else {
            binding.loadingProperties.visibility = GONE
            BaseFragment.properties.value?.let { properties ->
                if (properties.isEmpty()) {
                    binding.noData.visibility = VISIBLE
                }
            }
        }

        state.properties?.let { properties ->
            if(properties.isNotEmpty()) {
                if(properties != BaseFragment.properties.value) {
                    if(binding.noData.visibility == VISIBLE) {
                        binding.noData.visibility = GONE
                    }
                    if(binding.loadingProperties.visibility == VISIBLE) {
                        binding.loadingProperties.visibility = GONE
                    }
                    BaseFragment.properties.value = state.properties.toMutableList()
                }
            }
        }

        state.uiNotification?.let { uiNotification ->
            if(uiNotification == PROPERTIES_FULLY_UPDATED) {
                showMessage(this, resources.getString(R.string.property_update_totally))
            }

            if(uiNotification == PROPERTIES_FULLY_CREATED) {
                val mNotificationManager = AppNotificationManager(this)
                mNotificationManager.showNotification(null, resources.getString(R.string.property_create_totally))
            }
        }
    }

    fun showMessage(context: Context, message: String) {
        Snackbar.make(context, binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}