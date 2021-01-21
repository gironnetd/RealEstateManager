package com.codingwithmitch.espressodaggerexamples.util

import android.widget.ImageView
import com.google.firebase.storage.StorageReference
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.util.GlideManager
import javax.inject.Inject

@BrowseScope
class FakeGlideRequestManager
@Inject
constructor() : GlideManager {

    override fun setImage(imageUrl: String, imageView: ImageView) {
        // does nothing
    }

    override fun setImage(storageReference: StorageReference, imageView: ImageView) {
        // does nothing
    }
}