package com.openclassrooms.realestatemanager.extensions

import android.content.res.Resources
import android.graphics.Rect
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

fun DialogFragment.setWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun DialogFragment.setHeightPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentHeight = rect.height() * percent
    dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, percentHeight.toInt())
}

fun DialogFragment.setWidthAndHeightPercent(widthPercentage: Int, heightPercentage: Int) {
    val widthPercent = widthPercentage.toFloat() / 100
    val heightPercent = heightPercentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * widthPercent
    val percentHeight = rect.height() * heightPercent
    dialog?.window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
}

fun DialogFragment.setFullScreen() {
    dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
}
