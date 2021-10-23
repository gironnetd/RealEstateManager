package com.openclassrooms.realestatemanager.ui.property.edit

import com.openclassrooms.realestatemanager.ui.mvibase.MviViewState

data class PropertyEditViewState(
    val isSaved: Boolean = false,
    val inProgress: Boolean = false,
    val error: Throwable? = null,
    val uiNotification: UiNotification? = null
) : MviViewState {
    enum class UiNotification {
        PROPERTY_LOCALLY_UPDATED,
        PROPERTIES_FULLY_UPDATED,
        PROPERTY_LOCALLY_CREATED,
        PROPERTIES_FULLY_CREATED,
    }

    companion object {
        fun idle(): PropertyEditViewState {
            return PropertyEditViewState(
                isSaved = false,
                inProgress = false,
                error = null,
                uiNotification = null
            )
        }
    }
}
