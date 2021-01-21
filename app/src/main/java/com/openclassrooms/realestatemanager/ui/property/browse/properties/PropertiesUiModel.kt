package com.openclassrooms.realestatemanager.ui.property.browse.properties

import com.openclassrooms.realestatemanager.base.BaseViewState
import com.openclassrooms.realestatemanager.models.Property

sealed class PropertiesUiModel(
        val inProgress: Boolean = false,
        val properties: List<Property>? = null,
)
    : BaseViewState {

    object InProgress : PropertiesUiModel(true, null)

    object Failed : PropertiesUiModel()

    data class Success(private val result: List<Property>?) : PropertiesUiModel(false, result)

    class Idle : PropertiesUiModel(false, null)

}
