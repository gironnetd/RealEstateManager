package com.openclassrooms.realestatemanager.ui.property.search

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentMainSearchBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.navigation.browsedetail.BrowseDetailFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.edit.update.PropertyUpdateFragment
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PropertyDetailFragment
import com.openclassrooms.realestatemanager.ui.property.search.result.BrowseResultFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.shared.map.BaseMapFragment

class MainSearchFragment : BaseFragment(R.layout.fragment_main_search) {

    private var _binding: FragmentMainSearchBinding? = null
    val binding get() = _binding!!

    val mainActivity by lazy { activity as MainActivity }

    lateinit var mainSearchNavHostFragment: NavHostFragment
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var searchItem: MenuItem
    private lateinit var resultItem: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val contextThemeWrapper: Context = ContextThemeWrapper(activity, R.style.AppTheme_Tertiary)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        _binding = FragmentMainSearchBinding.inflate(localInflater, container, false)

        mainSearchNavHostFragment = childFragmentManager.findFragmentById(R.id.search_nav_fragment) as NavHostFragment
        mainSearchNavHostFragment.apply {
            val mainSearchNavigator = BrowseDetailFragmentNavigator(
                requireContext(),
                childFragmentManager,
                R.id.search_nav_fragment
            )
            navController.navigatorProvider.addNavigator(mainSearchNavigator)
            navController.setGraph(R.navigation.search_navigation)
        }

        super.onCreateView(localInflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        onBackPressedCallback()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        resultItem = menu.findItem(R.id.navigation_result_search)

        if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment is PropertySearchFragment) {
            (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment as PropertySearchFragment)
                .initResultMenuItem()
        }

        resultItem.isVisible = true

        // getting Linear Layout from custom layout
        val resultItemLayout = resultItem.actionView as LinearLayout

        resultItemLayout.apply {
            findViewById<TextView>(R.id.menu_item_title).text = resources.getString(R.string.search)
        }

        searchItem = menu.findItem(R.id.navigation_main_search)
        searchItem.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            with(mainActivity.binding) {
                toolBar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorTertiary, null))
                statusbar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorTertiaryDark, null))
            }
            if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                    is PropertySearchFragment
                ) {
                    if (mainActivity.binding.toolBar.visibility == GONE) {
                        mainActivity.binding.toolBar.visibility = VISIBLE
                    }
                    if (binding.toolBar.visibility == VISIBLE) {
                        binding.toolBar.visibility = GONE
                    }
                    mainActivity.onBackPressedCallback.isEnabled = true
                    onBackPressedCallback.isEnabled = false
                }

                if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                    is BrowseResultFragment
                ) {
                    if (mainActivity.binding.toolBar.visibility == VISIBLE) {
                        mainActivity.binding.toolBar.visibility = GONE
                    }
                    mainActivity.onBackPressedCallback.isEnabled = false

                    (
                        mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                            as BrowseResultFragment
                        )
                        .detail
                        .childFragmentManager
                        .primaryNavigationFragment?.let { primaryNavigationFragment ->
                            when (primaryNavigationFragment::class.java) {
                                PropertyDetailFragment::class.java -> {
                                    (primaryNavigationFragment as PropertyDetailFragment)
                                        .onBackPressedCallback.isEnabled = true
                                }
                                PropertyUpdateFragment::class.java -> {
                                    (primaryNavigationFragment as PropertyUpdateFragment)
                                        .onBackPressedCallback.isEnabled = true
                                }
                                else -> {
                                    onBackPressedCallback.isEnabled = true
                                }
                            }
                        }

                    with(
                        (
                            mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                                as BrowseResultFragment
                            )
                    ) {
                        if (this.detail.requireView().visibility == VISIBLE) {
                            if (detail.childFragmentManager.primaryNavigationFragment is BaseMapFragment) {
                                this.binding.toolBar.visibility = GONE
                                this@MainSearchFragment.binding.toolBar.visibility = VISIBLE
                            } else {
                                this.binding.toolBar.visibility = VISIBLE
                                this@MainSearchFragment.binding.toolBar.visibility = GONE
                            }
                        } else {
                            this.binding.toolBar.visibility = GONE
                            this@MainSearchFragment.binding.toolBar.visibility = VISIBLE
                        }
                    }
                }
            }

            if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) {
                if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                    is PropertySearchFragment
                ) {
                    if (mainActivity.binding.toolBar.visibility == GONE) {
                        mainActivity.binding.toolBar.visibility = VISIBLE
                    }
                    if (binding.toolBar.visibility == VISIBLE) {
                        binding.toolBar.visibility = GONE
                    }
                    mainActivity.onBackPressedCallback.isEnabled = true
                    onBackPressedCallback.isEnabled = false
                }

                if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                    is BrowseResultFragment
                ) {
                    if (mainActivity.binding.toolBar.visibility == VISIBLE) {
                        mainActivity.binding.toolBar.visibility = GONE
                    }
                    mainActivity.onBackPressedCallback.isEnabled = false

                    (
                        mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                            as BrowseResultFragment
                        )
                        .detail
                        .childFragmentManager
                        .primaryNavigationFragment?.let { primaryNavigationFragment ->
                            when (primaryNavigationFragment::class.java) {
                                PropertyDetailFragment::class.java -> {
                                    (primaryNavigationFragment as PropertyDetailFragment)
                                        .onBackPressedCallback.isEnabled = true
                                }
                                PropertyUpdateFragment::class.java -> {
                                    (primaryNavigationFragment as PropertyUpdateFragment)
                                        .onBackPressedCallback.isEnabled = true
                                }
                                else -> {
                                    onBackPressedCallback.isEnabled = true
                                }
                            }
                        }

                    with(
                        (
                            mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                                as BrowseResultFragment
                            )
                    ) {
                        if (detail.childFragmentManager.primaryNavigationFragment is BaseMapFragment) {
                            this.binding.toolBar.visibility = GONE
                            this@MainSearchFragment.binding.toolBar.visibility = VISIBLE
                        } else {
                            this.binding.toolBar.visibility = VISIBLE
                            this@MainSearchFragment.binding.toolBar.visibility = GONE
                        }
                    }
                }
            }
        } else {
            if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                is BrowseResultFragment
            ) {
                (
                    mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                        as BrowseResultFragment
                    )
                    .detail
                    .childFragmentManager
                    .primaryNavigationFragment?.let { primaryNavigationFragment ->
                        when (primaryNavigationFragment::class.java) {
                            PropertyDetailFragment::class.java -> {
                                (primaryNavigationFragment as PropertyDetailFragment)
                                    .onBackPressedCallback.isEnabled = false
                                if (onBackPressedCallback.isEnabled) {
                                    onBackPressedCallback.isEnabled = false
                                }
                            }
                            PropertyUpdateFragment::class.java -> {
                                (primaryNavigationFragment as PropertyUpdateFragment)
                                    .onBackPressedCallback.isEnabled = false
                                if (onBackPressedCallback.isEnabled) {
                                    onBackPressedCallback.isEnabled = false
                                }
                            }
                            else -> {
                                onBackPressedCallback.isEnabled = false
                            }
                        }
                    }
            }
        }
    }

    fun onBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                binding.toolBar.visibility = GONE
                mainSearchNavHostFragment.navController.navigate(R.id.navigation_search)
                mainActivity.binding.toolBar.visibility = VISIBLE
                BrowseResultFragment.searchedProperties.value = mutableListOf()
                isEnabled = false
                mainActivity.onBackPressedCallback.isEnabled = true

                if (mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                    is PropertySearchFragment
                ) {
                    (
                        mainSearchNavHostFragment.childFragmentManager.primaryNavigationFragment
                            as PropertySearchFragment
                        )
                        .initResultMenuItem()
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun initializeToolbar() {
        with(mainActivity.binding) {
            toolBar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorTertiary, null))
            statusbar.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorTertiaryDark, null))
        }
        if (mainActivity.binding.toolBar.visibility == GONE) {
            mainActivity.binding.toolBar.visibility = VISIBLE
        }
        appBarConfiguration = AppBarConfiguration.Builder(R.id.navigation_search).build()
        binding.toolBar.setupWithNavController(mainSearchNavHostFragment.navController, appBarConfiguration)

        if (!mainActivity.onBackPressedCallback.isEnabled) {
            mainActivity.onBackPressedCallback.isEnabled = true
        }

        binding.toolBar.setNavigationOnClickListener {
            binding.toolBar.visibility = GONE
            mainSearchNavHostFragment.navController.navigate(R.id.navigation_search)
            mainActivity.binding.toolBar.visibility = VISIBLE
            BrowseResultFragment.searchedProperties.value = mutableListOf()
        }
    }
}
