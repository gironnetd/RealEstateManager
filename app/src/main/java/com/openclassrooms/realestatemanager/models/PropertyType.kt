package com.openclassrooms.realestatemanager.models

enum class PropertyType(val type: String) {
    FLAT("Flat"),
    TOWNHOUSE("Townhouse"),
    PENTHOUSE("Penthouse"),
    HOUSE("House"),
    DUPLEX("Duplex"),
    NONE("None")
}