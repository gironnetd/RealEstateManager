package com.openclassrooms.realestatemanager.models

enum class PropertyStatus(val status: String) {
    SOLD("sold"),
    FOR_RENT("for rent"),
    IN_SALE("in sale"),
    NONE("None")
}