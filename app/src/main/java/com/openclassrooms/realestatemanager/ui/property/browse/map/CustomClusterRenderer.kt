package com.openclassrooms.realestatemanager.ui.property.browse.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

open class CustomClusterRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<CustomClusterItem>
) : DefaultClusterRenderer<CustomClusterItem>(context, map, clusterManager)