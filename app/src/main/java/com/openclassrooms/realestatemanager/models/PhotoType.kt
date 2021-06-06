package com.openclassrooms.realestatemanager.models

import androidx.annotation.StringRes
import androidx.room.TypeConverter
import com.openclassrooms.realestatemanager.R

enum class PhotoType constructor(@StringRes val type: Int) {
    MAIN(R.string.photo_type_main),
    BATHROOM(R.string.photo_type_bathroom),
    BEDROOM(R.string.photo_type_bedroom),
    FACADE(R.string.photo_type_facade),
    KITCHEN(R.string.photo_type_kitchen),
    LOUNGE(R.string.photo_type_lounge),
    NONE(R.string.photo_type_none)
}

class PhotoTypeConverter {
    @TypeConverter
    fun toPhotoType(type: String) = enumValueOf<PhotoType>(type)

    @TypeConverter
    fun fromPhotoType(photoType: PhotoType) = photoType.name
}