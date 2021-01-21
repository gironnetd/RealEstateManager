package com.openclassrooms.realestatemanager.models

data class Address(
        val street: String = "",
        val city: String = "",
        val postalCode: String = "",
        val country: String = "",
        val state: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
)
