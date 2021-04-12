package com.openclassrooms.realestatemanager.ui.property.browse.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.ui.property.BasePropertyFragment
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesIntent
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel
import com.openclassrooms.realestatemanager.ui.property.browse.shared.PropertiesUiModel.*
import com.openclassrooms.realestatemanager.util.GlideManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to list real estates.
 */
class PropertyListFragment
@Inject
constructor(
        viewModelFactory: ViewModelProvider.Factory,
        val requestManager: GlideManager,
) : BasePropertyFragment(R.layout.fragment_list, viewModelFactory), BaseView<PropertiesIntent, PropertiesUiModel> {

    private var _binding: FragmentListBinding? = null
    val binding get() = _binding!!

    private val loadConversationsIntentPublisher =
            PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    private lateinit var recyclerAdapter: PropertyListAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
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

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PropertyListFragment.context)
            recyclerAdapter = PropertyListAdapter(requestManager)
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