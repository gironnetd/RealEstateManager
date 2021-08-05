package com.openclassrooms.realestatemanager.ui.property

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowInsets
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.openclassrooms.realestatemanager.models.Property
import kotlin.properties.Delegates

abstract class BaseFragment
constructor(@LayoutRes private val layoutRes: Int): Fragment(layoutRes) {

        var screenWidth by Delegates.notNull<Int>()

        val masterWidthWeight = TypedValue()
        val detailWidthWeight = TypedValue()

        override fun onResume() {
                super.onResume()
                initializeToolbar()
        }

        abstract fun initializeToolbar()

        fun screenWidth(@NonNull activity: Activity): Int {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val windowMetrics = activity.windowManager.currentWindowMetrics
                        val insets: Insets = windowMetrics.windowInsets
                                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                        windowMetrics.bounds.width() - insets.left - insets.right
                } else {
                        val displayMetrics = DisplayMetrics()
                        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                        displayMetrics.widthPixels
                }
        }

        companion object {
                val properties: MutableLiveData<MutableList<Property>> = MutableLiveData<MutableList<Property>>()

        }

}