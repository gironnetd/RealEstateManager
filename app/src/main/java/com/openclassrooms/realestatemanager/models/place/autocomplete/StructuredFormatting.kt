package com.openclassrooms.realestatemanager.models.place.autocomplete

import com.google.gson.annotations.SerializedName

data class StructuredFormatting (
    @SerializedName(value = "main_text")
    val mainText: String,

    @SerializedName(value = "main_text_matched_substrings")
    val mainTextMatchedSubstrings: List<MatchedSubstring>,

    @SerializedName(value = "secondary_text")
    val secondaryText: String? = null
)
