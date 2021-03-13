package com.openclassrooms.realestatemanager.ui.property.browse.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.base.BaseView
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.browse.properties.PropertiesUiModel.*
import com.openclassrooms.realestatemanager.util.GlideManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Fragment to list real estates.
 */
class PropertiesFragment
@Inject
constructor(
        viewModelFactory: ViewModelProvider.Factory,
        private val requestManager: GlideManager,
) : Fragment(R.layout.fragment_list), BaseView<PropertiesIntent, PropertiesUiModel> {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val propertiesViewModel: PropertiesViewModel by viewModels {
        viewModelFactory
    }

    private val loadConversationsIntentPublisher =
            PublishSubject.create<PropertiesIntent.LoadPropertiesIntent>()
    private val refreshIntentPublisher = PublishSubject.create<PropertiesIntent.RefreshPropertiesIntent>()
    private val compositeDisposable = CompositeDisposable()

    private lateinit var recyclerAdapter: PropertiesAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        initRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compositeDisposable.add(propertiesViewModel.states().subscribe(this::render))
        propertiesViewModel.processIntents(intents())
    }

    override fun onResume() {
        super.onResume()
        loadConversationsIntentPublisher.onNext(PropertiesIntent.LoadPropertiesIntent)
    }

    override fun intents(): Observable<PropertiesIntent> {
        return Observable.merge(
                initialIntent(),
                loadPropertiesIntentPublisher(),
                refreshPropertiesIntentPublisher()
        )
    }

    private fun initialIntent(): Observable<PropertiesIntent.InitialIntent> {
        return Observable.just(PropertiesIntent.InitialIntent)
    }

    private fun loadPropertiesIntentPublisher(): Observable<PropertiesIntent.LoadPropertiesIntent> {
        return loadConversationsIntentPublisher
    }

    private fun refreshPropertiesIntentPublisher(): Observable<PropertiesIntent.RefreshPropertiesIntent> {
        return refreshIntentPublisher
    }

    override fun render(state: PropertiesUiModel) {
        when (state) {
            is InProgress -> {
                setUpScreenForLoadingState()
            }
            is Success -> {
                setUpScreenForSuccess(state.properties)
            }
            is Failed -> { }
            else -> { }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PropertiesFragment.context)
            recyclerAdapter = PropertiesAdapter(requestManager)
            adapter = recyclerAdapter
        }
    }

    private fun setUpScreenForSuccess(properties: List<Property>?) {
        if (properties != null && properties.isNotEmpty()) {
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