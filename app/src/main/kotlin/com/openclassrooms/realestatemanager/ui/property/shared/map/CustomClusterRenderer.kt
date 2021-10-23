package com.openclassrooms.realestatemanager.ui.property.shared.map

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.openclassrooms.realestatemanager.R

open class CustomClusterRenderer(
    val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<CustomClusterItem>
) : DefaultClusterRenderer<CustomClusterItem>(context, map, clusterManager) {

    override fun getColor(clusterSize: Int): Int {
        return ResourcesCompat.getColor(context.resources, R.color.colorPrimaryDark, null)
    }
}
