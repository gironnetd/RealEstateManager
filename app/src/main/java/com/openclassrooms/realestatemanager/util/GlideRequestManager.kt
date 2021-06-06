package com.openclassrooms.realestatemanager.util

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.StorageReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlideRequestManager
@Inject
constructor(private val requestManager: RequestManager) : GlideManager {

    override fun setImage(imageUrl: String, imageView: ImageView) {
        requestManager
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    override fun setImage(storageReference: StorageReference, imageView: ImageView, synchronized: Boolean ) {
        if(synchronized) {
            val futureBitmap = Glide.with(imageView.context)
                .asBitmap()
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .submit()
            val mainPhoto = futureBitmap.get()
            imageView.setImageDrawable(BitmapDrawable(imageView.context.resources, mainPhoto))
        } else {
            GlideApp.with(imageView.context)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
    }
}