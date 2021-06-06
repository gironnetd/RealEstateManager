package com.openclassrooms.realestatemanager.ui.property.browse.update

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.storageLocalDatabase
import java.io.File
import java.util.*

class PhotoUpdateAdapter : RecyclerView.Adapter<PhotoUpdateAdapter.PhotoViewHolder>() {

    interface OnItemClickListener {
        fun clickOnPhotoAtPosition(photoId: String)
    }

    var callBack: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) { callBack = listener }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Photo>() {

        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(from(parent.context).inflate(R.layout.layout_photo_list_item,
            parent,
            false),
            callBack)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(photos: List<Photo>?, ) {
        differ.submitList(photos)
    }

    class PhotoViewHolder
    constructor(
        itemView: View,
        var callBack: OnItemClickListener?,
    ) : RecyclerView.ViewHolder(itemView) {

        var photo: ImageView = itemView.findViewById(R.id.property_photo)
        var type: TextView = itemView.findViewById(R.id.property_type)

        fun bind(item: Photo) = with(itemView) {
            item.let { photo ->
                val localFile = File(photo.storageLocalDatabase(context, true))
                if(localFile.exists()) {
                    with(this@PhotoViewHolder.photo) {
                        setImageURI(null)
                        setImageURI(localFile.toUri())
                    }
                } else {
                    this@PhotoViewHolder.photo.setImageURI(null)
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
