package com.openclassrooms.realestatemanager.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.openclassrooms.realestatemanager.R
import java.util.*

object BitmapUtil {

    fun bitmapFromAsset(context: Context, fileName: String): Bitmap {
        val inputStream = context.assets.open(fileName + Constants.SLASH + Constants.THUMBNAIL_FILE_NAME)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap
    }

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

    fun bitmapDescriptorFromVector(context: Context, drawableId: Int): BitmapDescriptor? {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(Objects.requireNonNull(drawable!!)).mutate()
        }
        drawable = DrawableCompat.wrap(drawable!!).mutate()
        val bitmap = Bitmap.createBitmap(
                context.resources.getDimension(R.dimen.ic_marker_width).toInt(),
                context.resources.getDimension(R.dimen.ic_marker_height).toInt(),
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}