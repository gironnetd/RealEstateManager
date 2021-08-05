package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviResult

sealed class PropertyDetailResult : MviResult {

    sealed class PopulatePropertyResult : PropertyDetailResult() {
        data class Success(val property: Property) : PopulatePropertyResult()
        data class Failure(val error: Throwable) : PopulatePropertyResult()
        object InFlight : PopulatePropertyResult()
    }
}
