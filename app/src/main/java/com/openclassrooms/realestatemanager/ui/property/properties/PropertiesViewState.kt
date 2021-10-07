package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviViewState

data class PropertiesViewState(
    val inProgress: Boolean? = false,
    val properties: List<Property>? = null,
    val error: Throwable? = null,
    val uiNotification: UiNotification? = null
) : MviViewState {
    enum class UiNotification {
        PROPERTIES_FULLY_UPDATED,
        PROPERTIES_FULLY_CREATED
    }

    companion object {
        fun idle(): PropertiesViewState {
            return PropertiesViewState(
                inProgress = false,
                properties = null,
                error = null,
                uiNotification = null
            )
        }
    }
}
