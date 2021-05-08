package com.openclassrooms.realestatemanager.ui.property.browse.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class CustomClusterItem(
        lat: Double,
        lng: Double,
        private val title: String,
        private val snippet: String,
        private val tag: String
) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

    fun getTag(): String {
        return tag
    }

}