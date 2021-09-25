package com.openclassrooms.realestatemanager.ui.property.shared.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.setting.Currency
import com.openclassrooms.realestatemanager.ui.property.setting.Currency.EUROS
import com.openclassrooms.realestatemanager.ui.property.shared.BaseFragment
import com.openclassrooms.realestatemanager.util.Utils

class ListAdapter : RecyclerView.Adapter<ListAdapter.PropertyViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(propertyId: String)
    }

    private lateinit var callBack: OnItemClickListener

    fun setOnItemClickListener(listener: OnItemClickListener) { callBack = listener }

    private val diffCallback = object : DiffUtil.ItemCallback<Property>() {

        override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean {
            return oldItem == newItem
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        return PropertyViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.layout_property_list_item,
                parent, false
        ), if(::callBack.isInitialized) { callBack } else { null } )
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int { return differ.currentList.size }

    fun submitList(properties: List<Property>) {
        differ.submitList(properties)
        // Fix Bugs: Inconsistency detected. Invalid view holder adapter
        // by adding call to notifyDataSetChanged() method
        notifyDataSetChanged()
    }

    class PropertyViewHolder
    constructor(itemView: View, private var callBack: OnItemClickListener? = null ) : RecyclerView.ViewHolder(itemView) {

        var mainPhoto: ImageView = itemView.findViewById(R.id.property_main_photo)
        var type: TextView = itemView.findViewById(R.id.property_type)
        var street: TextView = itemView.findViewById(R.id.property_address_street)
        var price: TextView = itemView.findViewById(R.id.property_price)

        fun bind(item: Property) = with(itemView) {
            item.photos.singleOrNull { photo -> photo.mainPhoto }?.let { photo ->
                photo.bitmap?.let {
                    mainPhoto.setPadding(0,0,0,0)
                    mainPhoto.setImageBitmap(photo.bitmap)
                }
            }?: with(mainPhoto) {
                setImageBitmap(null)
                setPadding(20,20,20,20)
                setImageResource(R.drawable.ic_baseline_no_photography_24)
            }

            item.type.let {
                type.text = resources.getString(it.type).uppercase()
            }

            item.address.let {
                if(it.street.isNotEmpty()) {
                    if(resources.getBoolean(R.bool.isMasterDetail)) {
                        street.text = it.street
                    } else {
                        street.text = item.addressInList(resources)
                    }
                }
            }
            item.price.let {
                BaseFragment.defaultCurrency.value?.let { defaultCurrency ->
                    if(defaultCurrency == EUROS.currency) {
                        price.text = resources.getString(R.string.euros_symbol).plus(" $it")
                    }

                    if(defaultCurrency == Currency.DOLLARS.currency) {
                        price.text = resources.getString(R.string.dollars_symbol).plus(" ${Utils.convertEuroToDollar(it)}")
                    }
                }
            }



            itemView.setOnClickListener {
                callBack?.onItemClick(item.id)
            }
        }
    }
}