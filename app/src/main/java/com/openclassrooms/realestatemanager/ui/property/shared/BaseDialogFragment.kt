package com.openclassrooms.realestatemanager.ui.property.shared

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.openclassrooms.realestatemanager.R

open class BaseDialogFragment constructor(@LayoutRes private val layoutRes: Int): DialogFragment(layoutRes) {

    protected val none by lazy { resources.getString(R.string.none) }

    fun applyDialogDimension() {
        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            dialog!!.window!!.attributes.apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
            }.also {
                dialog!!.window!!.attributes = it as WindowManager.LayoutParams
            }
        }

        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            dialog!!.window!!.attributes.apply {
                width = (screenWidth(requireActivity()) * 0.8).toInt()
            }.also {
                dialog!!.window!!.attributes = it as WindowManager.LayoutParams
            }
        }
    }

    private fun screenWidth(@NonNull activity: Activity): Int {
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
}