package com.openclassrooms.realestatemanager.ui.property.update

import com.openclassrooms.realestatemanager.ui.mvibase.MviViewState

data class PropertyUpdateViewState(
    val isSaved: Boolean = false,
    val inProgress: Boolean = false,
    val error: Throwable? = null,
    val uiNotification: UiNotification? = null
): MviViewState {
    enum class UiNotification {
        PROPERTY_LOCALLY_UPDATED,
        PROPERTIES_FULLY_UPDATED,
    }

    companion object {
        fun idle(): PropertyUpdateViewState {
            return PropertyUpdateViewState(
                isSaved = false,
                inProgress = false,
                error = null,
                uiNotification = null
            )
        }
    }
}
