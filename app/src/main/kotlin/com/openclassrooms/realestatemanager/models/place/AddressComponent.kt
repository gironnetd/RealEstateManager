package com.openclassrooms.realestatemanager.models.place

import com.google.gson.annotations.SerializedName

data class AddressComponent(
    @SerializedName(value = "long_name")
    val longName: String,

    @SerializedName(value = "short_name")
    val shortName: String,

    val types: List<String>
)
