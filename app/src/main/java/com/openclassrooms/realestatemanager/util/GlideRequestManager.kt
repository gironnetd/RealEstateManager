package com.openclassrooms.realestatemanager.util

import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.StorageReference
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import javax.inject.Inject

@BrowseScope
class GlideRequestManager
@Inject
constructor(
        private val requestManager: RequestManager,
) : GlideManager {

    override fun setImage(imageUrl: String, imageView: ImageView) {
        requestManager
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
    }

    override fun setImage(storageReference: StorageReference, imageView: ImageView) {
        GlideApp.with(imageView.context)
                .load(storageReference)
                .into(imageView)
    }
}