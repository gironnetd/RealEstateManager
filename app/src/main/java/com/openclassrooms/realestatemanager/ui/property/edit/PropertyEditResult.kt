package com.openclassrooms.realestatemanager.ui.property.edit

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviResult

sealed class PropertyEditResult : MviResult {
    sealed class PopulatePropertyResult : PropertyEditResult() {
        data class Success(val property: Property) : PopulatePropertyResult()
        data class Failure(val error: Throwable) : PopulatePropertyResult()
        object InFlight : PopulatePropertyResult()
    }

    sealed class UpdatePropertyResult: PropertyEditResult() {
        data class Updated(val fullyUpdated: Boolean): UpdatePropertyResult()
        data class Failure(val error: Throwable) : UpdatePropertyResult()
        object InFlight : UpdatePropertyResult()
    }

    sealed class CreatePropertyResult: PropertyEditResult() {
        data class Created(val fullyCreated: Boolean): CreatePropertyResult()
        data class Failure(val error: Throwable) : CreatePropertyResult()
        object InFlight : CreatePropertyResult()
    }
}
