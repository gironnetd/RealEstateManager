package com.openclassrooms.realestatemanager.util

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
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

    override fun setImage(storageReference: StorageReference, imageView: ImageView, synchronized: Boolean ) {
        if(synchronized) {
                val futureBitmap = Glide.with(imageView.context)
                        .asBitmap()
                        .load(storageReference)
                        .submit()
                val mainPicture = futureBitmap.get()
                imageView.setImageDrawable(BitmapDrawable(imageView.context.resources, mainPicture))
        } else {
            GlideApp.with(imageView.context)
                    .load(storageReference)
                    .into(imageView)
        }
    }
}