package com.openclassrooms.realestatemanager.ui.property.propertydetail

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.ui.property.propertydetail.PhotoDetailAdapter.PhotoViewHolder
import java.util.*

class PhotoDetailAdapter : RecyclerView.Adapter<PhotoViewHolder>() {

    interface OnItemClickListener {
        fun clickOnPhotoAtPosition(photoId: String)
    }

    private var callBack: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) { callBack = listener }

    private val diffCallback = object : DiffUtil.ItemCallback<Photo>() {

        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(from(parent.context).inflate(R.layout.layout_photo_list_item,
            parent,
            false),
            callBack
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(photos: List<Photo>?) {
        differ.submitList(photos)
    }

    class PhotoViewHolder
    constructor(
        itemView: View,
        private var callBack: OnItemClickListener?,
    ) : RecyclerView.ViewHolder(itemView) {

        var photo: ImageView = itemView.findViewById(R.id.property_photo)
        var type: TextView = itemView.findViewById(R.id.property_type)

        fun bind(item: Photo) = with(itemView) {
            item.let { photo ->
                if(photo.bitmap != null) {
                    this@PhotoViewHolder.photo.setImageBitmap(photo.bitmap)
                    this@PhotoViewHolder.photo.setPadding(0,0,0,0)
                }

                if(photo.mainPhoto) {
                    type.text =  resources.getString(PhotoType.MAIN.type).uppercase(Locale.getDefault())
                } else if(photo.type != PhotoType.NONE) {
                    type.text = resources.getString(photo.type.type).uppercase(Locale.getDefault())
                }

                itemView.setOnClickListener {
                    callBack?.clickOnPhotoAtPosition(photo.id)
                }
            }
        }
    }
}
