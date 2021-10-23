package com.openclassrooms.realestatemanager.models.place.detail

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName(value = "author_name")
    val authorName: String,

    @SerializedName(value = "author_url")
    val authorURL: String,

    val language: String,

    @SerializedName(value = "profile_photo_url")
    val profilePhotoURL: String,

    val rating: Long,

    @SerializedName(value = "relative_time_description")
    val relativeTimeDescription: String,

    val text: String,
    val time: Long
)
