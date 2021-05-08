package com.openclassrooms.realestatemanager.ui.property.browse.list

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat.START
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.ui.MainActivity
import com.openclassrooms.realestatemanager.ui.property.BaseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.BrowseFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel.*
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesViewModel
import com.openclassrooms.realestatemanager.util.GlideManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to list real estates.
 */
class ListFragment
@Inject constructor() : BaseFragment(R.layout.fragment_list, null), BaseView<PropertiesIntent, PropertiesUiModel> {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var requestManager: GlideManager

    private val propertiesViewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!

    private val loadConversationsIntentPublisher =
            PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    private lateinit var recyclerAdapter: ListAdapter

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).browseComponent()
                .inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        configureView()
        initRecyclerView()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onResume() {
        super.onResume()
        if(properties.isNotEmpty()) {
            setUpScreenForSuccess()
        } else {
            compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
            propertiesViewModel.processIntents(intents())
        }
    }

    override fun initializeToolbar() {
        this.parentFragment?.parentFragment?.let {
            val browseFragment = this.parentFragment as BrowseFragment

            browseFragment.binding.toolBar.setNavigationOnClickListener {
                val mainActivity = activity as MainActivity
                if (!mainActivity.binding.drawerLayout.isDrawerOpen(START)) {
                    mainActivity.binding.drawerLayout.openDrawer(START)
                } else {
                    mainActivity.binding.drawerLayout.closeDrawer(START)
                }
            }
        }
    }

    override fun intents(): Observable<PropertiesIntent> {
        return Observable.merge(initialIntent(), loadPropertiesIntentPublisher()
        )
    }

    private fun initialIntent(): Observable<PropertiesIntent.InitialIntent> {
        return Observable.just(PropertiesIntent.InitialIntent)
    }

    private fun loadPropertiesIntentPublisher(): Observable<PropertiesIntent.LoadPropertiesIntent> {
        return loadConversationsIntentPublisher
    }

    override fun render(state: PropertiesUiModel) {
        when  {
            state.inProgress -> {
                setUpScreenForLoadingState()
            }
            state is Success -> {
                if(properties.isEmpty() && state.properties!!.isNotEmpty()) {
                    properties.addAll(state.properties)
                }

                if(properties != state.properties) {
                    properties.clear()
                    properties.addAll(state.properties!!)
                }
                setUpScreenForSuccess()
            }
            state is Failed -> { }
            state is Idle -> { }
            else -> { }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureView()
    }

    private fun configureView() {
        this.parentFragment?.let {
            val masterLayoutParams = FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)

            val listLayoutParams = binding.recyclerView.layoutParams as ConstraintLayout.LayoutParams

            if (resources.getBoolean(R.bool.isMasterDetail)) {

                val displayMetrics = DisplayMetrics()
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels

                val masterWidthWeight = TypedValue()
                resources.getValue(R.dimen.master_width_weight, masterWidthWeight, false)

                masterLayoutParams.width = (screenWidth * masterWidthWeight.float).toInt()
                binding.listFragment.layoutParams = masterLayoutParams

                listLayoutParams.topMargin = 0

                binding.recyclerView.layoutParams = listLayoutParams
                binding.recyclerView.requestLayout()
            } else if(!resources.getBoolean(R.bool.isMasterDetail)) {

                masterLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

                binding.listFragment.layoutParams = masterLayoutParams
                binding.listFragment.requestLayout()

                listLayoutParams.topMargin = resources.getDimension(R.dimen.list_properties_margin_top).toInt()
                binding.recyclerView.layoutParams = listLayoutParams
                binding.recyclerView.requestLayout()
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ListFragment.context)
            recyclerAdapter = ListAdapter(requestManager)
            adapter = recyclerAdapter
        }
    }

    private fun setUpScreenForSuccess() {
        if (properties.isNotEmpty()) {
            binding.recyclerView.visibility = VISIBLE
            binding.noDataTextView.visibility = GONE

            if (!::recyclerAdapter.isInitialized) {
                initRecyclerView()
            }
            recyclerAdapter.apply {
                submitList(properties = properties)
                notifyDataSetChanged()
            }
        } else {
            binding.recyclerView.visibility = GONE
            binding.noDataTextView.visibility = VISIBLE
        }
    }

    private fun setUpScreenForLoadingState() {
        binding.recyclerView.visibility = GONE
        binding.noDataTextView.visibility = VISIBLE
    }
}