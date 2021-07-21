package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseResult
import com.openclassrooms.realestatemanager.models.Property

sealed class PropertiesResult : BaseResult {

    sealed class LoadPropertiesResult : PropertiesResult() {
        data class Success(val properties: List<Property>) : LoadPropertiesResult()
        data class Failure(val error: Throwable) : LoadPropertiesResult()
        object InFlight : LoadPropertiesResult()
    }

    sealed class UpdatePropertyResult: PropertiesResult() {
        data class Updated(val fullyUpdated: Boolean): UpdatePropertyResult()
        data class Failure(val error: Throwable) : UpdatePropertyResult()
        object InFlight : UpdatePropertyResult()
    }
}
