package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviResult

sealed class PropertiesResult : MviResult {

    sealed class LoadPropertiesResult : PropertiesResult() {
        data class Success(val properties: List<Property>?) : LoadPropertiesResult()
        data class Failure(val error: Throwable) : LoadPropertiesResult()
        object InFlight : LoadPropertiesResult()
    }

    sealed class UpdatePropertyResult : PropertiesResult() {
        data class Updated(val fullyUpdated: Boolean) : UpdatePropertyResult()
        data class Failure(val error: Throwable) : UpdatePropertyResult()
        object InFlight : UpdatePropertyResult()
    }

    sealed class CreatePropertyResult : PropertiesResult() {
        data class Created(val fullyCreated: Boolean) : CreatePropertyResult()
        data class Failure(val error: Throwable) : CreatePropertyResult()
        object InFlight : CreatePropertyResult()
    }
}
