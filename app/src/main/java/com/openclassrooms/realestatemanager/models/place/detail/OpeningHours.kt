package com.openclassrooms.realestatemanager.models.place.detail

import com.google.gson.annotations.SerializedName

data class OpeningHours (
    @SerializedName(value = "open_now")
    val openNow: Boolean,

    val periods: List<Period>,

    @SerializedName(value = "weekday_text")
    val weekdayText: List<String>
)