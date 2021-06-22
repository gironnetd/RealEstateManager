package com.openclassrooms.realestatemanager.util

import android.graphics.Bitmap

object BitmapUtil {

    fun sameAs(A: Bitmap, B: Bitmap): Boolean {
        // Different types of image
        if (A.config != B.config) return false

        // Different sizes
        if (A.width != B.width) return false
        if (A.height != B.height) return false

        // Allocate arrays - OK because at worst we have 3 bytes + Alpha (?)
        val w = A.width
        val h = A.height
        val argbA = IntArray(w * h)
        val argbB = IntArray(w * h)
        A.getPixels(argbA, 0, w, 0, 0, w, h)
        B.getPixels(argbB, 0, w, 0, 0, w, h)

        // Alpha channel special check
        if (A.config == Bitmap.Config.ALPHA_8) {
            // in this case we have to manually compare the alpha channel as the rest is garbage.
            val length = w * h
            for (i in 0 until length) {
                if (argbA[i] and -0x1000000 != argbB[i] and -0x1000000) {
                    return false
                }
            }
            return true
        }
        return argbA.contentEquals(argbB)
    }
}