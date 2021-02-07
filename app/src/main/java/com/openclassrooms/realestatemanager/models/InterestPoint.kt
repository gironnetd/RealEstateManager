package com.openclassrooms.realestatemanager.models

import androidx.room.TypeConverter

enum class InterestPoint(val place: String) {
    SCHOOL("School"),
    PLAYGROUND("Playground"),
    SHOP("Shop"),
    BUSES("Buses"),
    SUBWAY("Subway"),
    PARK("Park")
}

class InterestPointConverter {

    @TypeConverter
    fun interestPointsToString(interestPoints: MutableList<InterestPoint>?): String? =
            interestPoints?.joinToString(separator = SEPARATOR) { it.name }

    @TypeConverter
    fun stringToInterestPoints(interestPoints: String?): MutableList<InterestPoint>? =
            interestPoints?.split(SEPARATOR)?.map { InterestPoint.valueOf(it) }?.toMutableList()

    companion object {
        private const val SEPARATOR: String = ";"
    }
}