package com.openclassrooms.realestatemanager.ui.property.update

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviResult

sealed class PropertyUpdateResult : MviResult {
    sealed class PopulatePropertyResult : PropertyUpdateResult() {
        data class Success(val property: Property) : PopulatePropertyResult()
        data class Failure(val error: Throwable) : PopulatePropertyResult()
        object InFlight : PopulatePropertyResult()
    }

    sealed class UpdatePropertyResult: PropertyUpdateResult() {
        data class Updated(val fullyUpdated: Boolean): UpdatePropertyResult()
        data class Failure(val error: Throwable) : UpdatePropertyResult()
        object InFlight : UpdatePropertyResult()
    }
}
