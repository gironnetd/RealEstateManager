package com.openclassrooms.realestatemanager.models.place.autocomplete

import com.google.gson.annotations.SerializedName

data class Prediction (
    val description: String,

    @SerializedName(value = "matched_substrings")
    val matchedSubstrings: List<MatchedSubstring>,

    @SerializedName(value = "place_id")
    val placeID: String,

    val reference: String,

    @SerializedName(value = "structured_formatting")
    val structuredFormatting: StructuredFormatting,

    val terms: List<Term>,
    val types: List<String>
)
