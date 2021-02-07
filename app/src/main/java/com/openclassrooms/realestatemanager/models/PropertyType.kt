package com.openclassrooms.realestatemanager.models

import androidx.room.TypeConverter

enum class PropertyType(val type: String) {
    FLAT("Flat"),
    TOWNHOUSE("Townhouse"),
    PENTHOUSE("Penthouse"),
    HOUSE("House"),
    DUPLEX("Duplex"),
    NONE("None")
}

class PropertyTypeConverter {
    @TypeConverter
    fun toPropertyType(type: String) = enumValueOf<PropertyType>(type)

    @TypeConverter
    fun fromPropertyType(propertyType: PropertyType) = propertyType.name
}