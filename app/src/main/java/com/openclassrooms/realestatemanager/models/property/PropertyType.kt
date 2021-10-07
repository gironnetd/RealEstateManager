package com.openclassrooms.realestatemanager.models.property

import androidx.annotation.StringRes
import androidx.room.TypeConverter
import com.openclassrooms.realestatemanager.R

enum class PropertyType constructor(@StringRes val type: Int) {
    FLAT(R.string.property_type_flat),
    TOWNHOUSE(R.string.property_type_townhouse),
    PENTHOUSE(R.string.property_type_penthouse),
    HOUSE(R.string.property_type_house),
    DUPLEX(R.string.property_type_duplex),
    NONE(R.string.property_type_none)
}

class PropertyTypeConverter {
    @TypeConverter
    fun toPropertyType(type: String) = enumValueOf<PropertyType>(type)

    @TypeConverter
    fun fromPropertyType(propertyType: PropertyType) = propertyType.name
}