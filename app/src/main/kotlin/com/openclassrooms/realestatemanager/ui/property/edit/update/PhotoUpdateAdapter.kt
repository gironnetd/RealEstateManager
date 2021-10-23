package com.openclassrooms.realestatemanager.ui.property.edit.update

import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.PhotoType
import java.util.*

class PhotoUpdateAdapter(val context: Context) : RecyclerView.Adapter<PhotoUpdateAdapter.PhotoViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Photo>() {

        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)

    interface OnItemClickListener {
        fun clickOnPhotoAtPosition(photoId: String)
    }

    private var callBack: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) { callBack = listener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            from(context).inflate(
                R.layout.layout_photo_list_item,
                parent,
                false
            ),
            callBack
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(photos: List<Photo>) {
        differ.submitList(photos)
        notifyDataSetChanged()
    }

    fun clear() {
        differ.submitList(null)
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
                photo.bitmap?.let {
                    this@PhotoViewHolder.photo.setPadding(0, 0, 0, 0)
                    this@PhotoViewHolder.photo.setImageBitmap(photo.bitmap)
                } ?: with(this@PhotoViewHolder.photo) {
                    setImageBitmap(null)
                    setPadding(emptyPhotoPadding, emptyPhotoPadding, emptyPhotoPadding, emptyPhotoPadding)
                    setImageResource(R.drawable.ic_baseline_no_photography_24)
                }

                if (photo.mainPhoto) {
                    type.text = resources.getString(PhotoType.MAIN.type).uppercase(Locale.getDefault())
                } else if (photo.type != PhotoType.NONE) {
                    type.text = resources.getString(photo.type.type).uppercase(Locale.getDefault())
                }

                itemView.setOnClickListener {
                    callBack?.clickOnPhotoAtPosition(photo.id)
                }
            }
        }
    }

    companion object {
        const val emptyPhotoPadding = 32
    }
}
