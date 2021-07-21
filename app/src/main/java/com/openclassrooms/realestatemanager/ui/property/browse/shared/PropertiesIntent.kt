package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseIntent
import com.openclassrooms.realestatemanager.models.Property

sealed class PropertiesIntent : BaseIntent {

    object InitialIntent : PropertiesIntent()

    object LoadPropertiesIntent : PropertiesIntent()

    data class UpdatePropertyIntent(val property: Property) : PropertiesIntent()
}
