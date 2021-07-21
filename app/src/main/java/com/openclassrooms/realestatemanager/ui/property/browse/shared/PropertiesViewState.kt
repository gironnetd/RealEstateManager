package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseViewState
import com.openclassrooms.realestatemanager.models.Property

data class PropertiesViewState(
        val inProgress: Boolean = false,
        val properties: List<Property>? = null,
        val error: Throwable? = null,
        val uiNotification: UiNotification? = null
) : BaseViewState {
    enum class UiNotification {
        PROPERTY_PARTIALLY_UPDATED,
        PROPERTIES_FULLY_UPDATED,
    }

    companion object {
        fun idle(): PropertiesViewState {
            return PropertiesViewState(
                inProgress = false,
                properties = null,
                uiNotification = null
            )
        }
    }

//    object InProgress : PropertiesViewState(true, null)
//
//    object Failed : PropertiesViewState()
//
//    data class Success(private val result: List<Property>?) : PropertiesViewState(false, result)
//
//    class Idle : PropertiesViewState(false, null)

}
