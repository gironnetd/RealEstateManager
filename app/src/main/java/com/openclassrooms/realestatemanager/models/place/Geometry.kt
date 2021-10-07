package com.openclassrooms.realestatemanager.models.place

import com.openclassrooms.realestatemanager.models.place.detail.Location
import com.openclassrooms.realestatemanager.models.place.detail.Viewport

data class Geometry (
    val location: Location,
    val viewport: Viewport
)