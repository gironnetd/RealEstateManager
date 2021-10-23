package com.openclassrooms.realestatemanager.models.place

import com.google.gson.annotations.SerializedName

data class PlusCode(
    @SerializedName(value = "compound_code")
    val compoundCode: String,

    @SerializedName(value = "global_code")
    val globalCode: String
)
