package com.openclassrooms.realestatemanager.ui.property.shared

import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property
import kotlin.properties.Delegates

abstract class BaseFragment
constructor(@LayoutRes private val layoutRes: Int): Fragment(layoutRes) {

        protected val none by lazy { resources.getString(R.string.none) }
        protected val colorPrimary by lazy { ContextCompat.getColor(requireContext(), R.color.colorPrimary) }

        var screenWidth by Delegates.notNull<Int>()
        val masterWidthWeight = TypedValue()
        val detailWidthWeight = TypedValue()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                initializeToolbar()
                return super.onCreateView(inflater, container, savedInstanceState)
        }

        abstract fun initializeToolbar()

        fun screenWidth(@NonNull activity: Activity): Int {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val windowMetrics = activity.windowManager.currentWindowMetrics
                        val insets: Insets = windowMetrics.windowInsets
                                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                        windowMetrics.bounds.width() - insets.left - insets.right
                } else {
                        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                        val displayMetrics = DisplayMetrics()
                        display.getRealMetrics(displayMetrics)
                        displayMetrics.widthPixels
                }
        }

        companion object {
                val properties: MutableLiveData<MutableList<Property>> = MutableLiveData<MutableList<Property>>()
        }
}