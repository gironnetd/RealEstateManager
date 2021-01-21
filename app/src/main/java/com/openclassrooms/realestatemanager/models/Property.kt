package com.openclassrooms.realestatemanager.models

import java.util.*

data class Property(
        var id: String = "",
        val type: PropertyType = PropertyType.NONE,
        val price: Int = 0,
        val surface: Int = 0,
        val rooms: Int = 0,
        val bedRooms: Int = 0,
        val bathRooms: Int = 0,
        val description: String = "",
        val address: Address? = null,
        val interestPoints: List<InterestPoint> = arrayListOf(),
        val status: PropertyStatus = PropertyStatus.IN_SALE,
        var agentId: String? = null,
        var mainPicture: Picture? = null,
        val entryDate: Date = Date(),
        val soldDate: Date? = null,
)
