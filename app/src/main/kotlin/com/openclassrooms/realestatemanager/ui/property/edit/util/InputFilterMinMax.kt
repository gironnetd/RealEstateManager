package com.openclassrooms.realestatemanager.ui.property.edit.util

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax constructor(private var min: Long, private var max: Long) : InputFilter {

    constructor(min: String, max: String) : this(min.toLong(), max.toLong())

    override fun filter(
        source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toLong()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
            print(nfe.localizedMessage)
        }
        return ""
    }

    private fun isInRange(a: Long, b: Long, c: Long): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}
