package com.openclassrooms.realestatemanager.models

import androidx.room.TypeConverter

enum class PictureType(val type: String) {
    MAIN("main"),
    BATHROOM("bathroom"),
    BEDROOM("bedroom"),
    FACADE("facade"),
    KITCHEN("kitchen"),
    LOUNGE("lounge"),
    NONE("none")
}

class PictureTypeConverter {
    @TypeConverter
    fun toPictureType(type: String) = enumValueOf<PictureType>(type)

    @TypeConverter
    fun fromPictureType(pictureType: PictureType) = pictureType.name
}