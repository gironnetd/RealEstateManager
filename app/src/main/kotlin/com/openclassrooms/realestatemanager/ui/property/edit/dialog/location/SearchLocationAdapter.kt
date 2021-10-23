package com.openclassrooms.realestatemanager.ui.property.edit.dialog.location

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.openclassrooms.realestatemanager.R

class SearchLocationAdapter(val context: Context) : RecyclerView.Adapter<SearchLocationAdapter.LocationViewHolder>() {

    interface SearchListener {
        fun onSearchItemClick(placeId: String)
    }

    private val diffCallback = object : DiffUtil.ItemCallback<AutocompletePrediction>() {

        override fun areItemsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
            return oldItem.placeId == newItem.placeId
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    private var callBack: SearchListener? = null

    fun setCallBack(listener: SearchListener) { callBack = listener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LocationViewHolder(
            from(context).inflate(
                R.layout.layout_search_location_item,
                parent, false
            ),
            callBack
        )
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(prediction: List<AutocompletePrediction>) {
        differ.submitList(prediction)
        notifyDataSetChanged()
    }

    fun clear() {
        differ.submitList(null)
    }

    class LocationViewHolder
    constructor(itemView: View, private var callBack: SearchListener?) : RecyclerView.ViewHolder(itemView) {

        var fullText: TextView = itemView.findViewById(R.id.tv_search_result_address)

        fun bind(item: AutocompletePrediction) = with(itemView) {
            item.let { prediction ->
                val stringBuilder = SpannableStringBuilder()

                val primaryText = SpannableString(prediction.getPrimaryText(null))
                primaryText.setSpan(StyleSpan(BOLD), 0, primaryText.length, 0)
                stringBuilder.append(primaryText)

                if (prediction.getSecondaryText(null).toString().isNotEmpty()) {
                    stringBuilder.append(", ")

                    val secondaryText = SpannableString(prediction.getSecondaryText(null))
                    secondaryText.setSpan(StyleSpan(NORMAL), 0, secondaryText.length, 0)
                    secondaryText.setSpan(ForegroundColorSpan(Color.DKGRAY), 0, secondaryText.length, 0)
                    stringBuilder.append(secondaryText)
                }

                fullText.setText(stringBuilder, TextView.BufferType.SPANNABLE)

                setOnClickListener {
                    callBack?.onSearchItemClick(prediction.placeId)
                }
            }
        }
    }
}
