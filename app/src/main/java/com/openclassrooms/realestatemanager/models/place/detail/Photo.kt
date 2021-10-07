package com.openclassrooms.realestatemanager.models.place.detail

import com.google.gson.annotations.SerializedName

data class Photo (
    val height: Long,

    @SerializedName(value = "html_attributions")
    val htmlAttributions: List<String>,

    @SerializedName(value = "photo_reference")
    val photoReference: String,

    val width: Long
)