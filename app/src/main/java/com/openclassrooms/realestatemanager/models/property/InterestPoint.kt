package com.openclassrooms.realestatemanager.models.property

import androidx.annotation.StringRes
import androidx.room.TypeConverter
import com.openclassrooms.realestatemanager.R

enum class InterestPoint constructor(@StringRes val place: Int) {
    SCHOOL(R.string.interest_point_school),
    PLAYGROUND(R.string.interest_point_playground),
    SHOP(R.string.interest_point_shop),
    BUSES(R.string.interest_point_buses),
    SUBWAY(R.string.interest_point_subway),
    PARK(R.string.interest_point_park),
    HOSPITAL(R.string.interest_point_hospital),
    RESTAURANTS(R.string.interest_point_restaurants),
    GAS_STATIONS(R.string.interest_point_gas_stations),
    NONE(R.string.interest_point_none)
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