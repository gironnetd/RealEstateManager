package com.openclassrooms.realestatemanager.util

import android.widget.ImageView
import com.google.firebase.storage.StorageReference

interface GlideManager {

    fun setImage(imageUrl: String, imageView: ImageView)

    fun setImage(storageReference: StorageReference, imageView: ImageView)
}