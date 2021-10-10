package com.openclassrooms.realestatemanager.ui.property.search.result

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commitNow
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.FragmentBrowseBinding
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.fragments.search.result.ResultSearchDetailNavHostFragment
import com.openclassrooms.realestatemanager.ui.navigation.browsedetail.BrowseDetailFragmentNavigator
import com.openclassrooms.realestatemanager.ui.property.search.result.list.SearchListFragment
import com.openclassrooms.realestatemanager.ui.property.shared.BaseBrowseFragment

class BrowseResultFragment : BaseBrowseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val contextThemeWrapper: Context = ContextThemeWrapper(mainActivity, R.style.AppTheme_Tertiary)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        _binding = FragmentBrowseBinding.inflate(localInflater, container, false)

        childFragmentManager.commitNow {
            setReorderingAllowed(true)
            replace(R.id.result_list_fragment, SearchListFragment())
        }
        master = childFragmentManager.findFragmentById(R.id.result_list_fragment) as SearchListFragment

        childFragmentManager.commitNow {
            setReorderingAllowed(true)
            replace(R.id.result_detail_nav_fragment, ResultSearchDetailNavHostFragment())
        }
        detail = childFragmentManager.findFragmentById(R.id.result_detail_nav_fragment) as NavHostFragment
        val detailNavigator = BrowseDetailFragmentNavigator(requireContext(), detail.childFragmentManager, R.id.result_detail_nav_fragment)

        detail.apply {
            navController.navigatorProvider.addNavigator(detailNavigator)
            navController.setGraph(R.navigation.search_result_detail_navigation)
        }
        super.onCreateView(localInflater, container, savedInstanceState)
        return binding.root
    }

    override fun isDetailFragmentSelected(): Boolean {
        return WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED
    }

    override fun setDetailFragmentSelected(isSelected: Boolean) {
        WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED = isSelected
    }

    companion object {
        var WHEN_NORMAL_MODE_IS_DETAIL_FRAGMENT_SELECTED: Boolean = false
        val searchedProperties: MutableLiveData<MutableList<Property>> = MutableLiveData<MutableList<Property>>()
    }
}