package com.openclassrooms.realestatemanager.models

import androidx.room.TypeConverter

enum class PropertyStatus(val status: String) {
    SOLD("sold"),
    FOR_RENT("for rent"),
    IN_SALE("in sale"),
    NONE("None")
}

class PropertyStatusConverter {
    @TypeConverter
    fun toPropertyStatus(status: String) = enumValueOf<PropertyStatus>(status)

    @TypeConverter
    fun fromPropertyStatus(propertyStatus: PropertyStatus) = propertyStatus.name
}