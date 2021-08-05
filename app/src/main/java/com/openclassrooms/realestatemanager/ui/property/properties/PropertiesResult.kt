package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviResult

sealed class PropertiesResult : MviResult {
    sealed class LoadPropertiesResult : PropertiesResult() {
        data class Success(val haveBeenFullyUpdated: Boolean? = false, val properties: List<Property>?) : LoadPropertiesResult()
        data class Failure(val error: Throwable) : LoadPropertiesResult()
        object InFlight : LoadPropertiesResult()
    }
}
