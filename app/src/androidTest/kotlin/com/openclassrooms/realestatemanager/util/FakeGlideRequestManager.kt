package com.openclassrooms.realestatemanager.util

import android.widget.ImageView
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGlideRequestManager
@Inject
constructor() : GlideManager {

    override fun setImage(imageUrl: String, imageView: ImageView) {
        // does nothing
    }

    override fun setImage(storageReference: StorageReference, imageView: ImageView, synchronized: Boolean) {
        // does nothing
    }
}