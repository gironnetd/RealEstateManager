package com.openclassrooms.realestatemanager.ui.property.browse.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.models.storageUrl
import com.openclassrooms.realestatemanager.ui.property.browse.detail.PictureAdapter.PictureViewHolder
import com.openclassrooms.realestatemanager.util.GlideManager

class PictureAdapter(
        private val requestManager: GlideManager,
) : RecyclerView.Adapter<PictureViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Picture>() {

        override fun areItemsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        return PictureViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_picture_list_item,
                        parent,
                        false
                ),
                requestManager
        )
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(
            pictures: List<Picture>?,
    ) {
        differ.submitList(pictures)
    }

    class PictureViewHolder
    constructor(
            itemView: View,
            private val requestManager: GlideManager,
    ) : RecyclerView.ViewHolder(itemView) {

        var picture: ImageView = itemView.findViewById(R.id.property_picture)
        var type: TextView = itemView.findViewById(R.id.property_type)

        fun bind(item: Picture) = with(itemView) {
            item.let { picture ->
                val gsReference = Firebase.storage.getReferenceFromUrl(picture.storageUrl(isThumbnail = true))
                requestManager.setImage(gsReference, this@PictureViewHolder.picture, false)
            }
            type.text = item.type.type.toUpperCase()
        }
    }
}