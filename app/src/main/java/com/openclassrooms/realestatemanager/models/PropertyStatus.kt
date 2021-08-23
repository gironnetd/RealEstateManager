package com.openclassrooms.realestatemanager.models

import androidx.annotation.StringRes
import androidx.room.TypeConverter
import com.openclassrooms.realestatemanager.R

enum class PropertyStatus constructor(@StringRes val status: Int) {
    SOLD(R.string.property_status_sold),
    FOR_RENT(R.string.property_status_for_rent),
    IN_SALE(R.string.property_status_in_sale),
    NONE(R.string.property_status_none)
}

class PropertyStatusConverter {
    @TypeConverter
    fun toPropertyStatus(status: String) = enumValueOf<PropertyStatus>(status)

    @TypeConverter
    fun fromPropertyStatus(propertyStatus: PropertyStatus) = propertyStatus.name
}