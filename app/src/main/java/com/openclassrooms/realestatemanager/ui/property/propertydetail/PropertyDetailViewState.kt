package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewState

data class PropertyDetailViewState(
    val inProgress: Boolean = false,
    val property: Property? = null,
    val error: Throwable? = null,
) : MviViewState {

    companion object {
        fun idle(): PropertyDetailViewState {
            return PropertyDetailViewState(
                inProgress = false,
                property = null,
            )
        }
    }
}
