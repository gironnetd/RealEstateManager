package com.openclassrooms.realestatemanager.models.place.detail

import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.models.place.AddressComponent
import com.openclassrooms.realestatemanager.models.place.Geometry
import com.openclassrooms.realestatemanager.models.place.PlusCode

data class PlaceDetails(
    @SerializedName(value = "address_components")
    val addressComponents: List<AddressComponent>,

    @SerializedName(value = "adr_address")
    val adrAddress: String,

    @SerializedName(value = "business_status")
    val businessStatus: String,

    @SerializedName(value = "formatted_address")
    val formattedAddress: String,

    @SerializedName(value = "formatted_phone_number")
    val formattedPhoneNumber: String,

    val geometry: Geometry,
    val icon: String,

    @SerializedName(value = "icon_background_color")
    val iconBackgroundColor: String,

    @SerializedName(value = "icon_mask_base_uri")
    val iconMaskBaseURI: String,

    @SerializedName(value = "international_phone_number")
    val internationalPhoneNumber: String,

    val name: String,

    @SerializedName(value = "opening_hours")
    val openingHours: OpeningHours,

    val photos: List<Photo>,

    @SerializedName(value = "place_id")
    val placeID: String,

    @SerializedName(value = "plus_code")
    val plusCode: PlusCode,

    val rating: Double,
    val reference: String,
    val reviews: List<Review>,
    val types: List<String>,
    val url: String,

    @SerializedName(value = "user_ratings_total")
    val userRatingsTotal: Long,

    @SerializedName(value = "utc_offset")
    val utcOffset: Long,

    val vicinity: String,
    val website: String
)
