package com.openclassrooms.realestatemanager.ui.property.browse.properties

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
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.storageUrl
import com.openclassrooms.realestatemanager.util.GlideManager

class PropertiesAdapter(
        private val requestManager: GlideManager,
) : RecyclerView.Adapter<PropertiesAdapter.PropertyViewHolder>() {

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Property>() {

        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        return PropertyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_property_list_item,
                        parent,
                        false
                ),
                requestManager
        )
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(
            properties: List<Property>?,

            ) {
        differ.submitList(properties)
    }

    class PropertyViewHolder
    constructor(
            itemView: View,
            private val requestManager: GlideManager,
    ) : RecyclerView.ViewHolder(itemView) {

        var mainPicture: ImageView = itemView.findViewById(R.id.property_main_picture)
        var type: TextView = itemView.findViewById(R.id.property_type)
        var street: TextView = itemView.findViewById(R.id.property_address_street)
        var price: TextView = itemView.findViewById(R.id.property_price)

        fun bind(item: Property) = with(itemView) {
            item.mainPicture?.let { picture ->
                picture.propertyId = item.id
                val gsReference = Firebase.storage.getReferenceFromUrl(picture.storageUrl(isThumbnail = true))
                requestManager.setImage(gsReference, mainPicture)
            }

            item.type.let { type.text = it.type }
            item.address?.let { street.text = it.street }
            item.price.let { price.text = "$".plus("$it") }
        }
    }
}