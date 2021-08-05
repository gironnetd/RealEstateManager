package com.openclassrooms.realestatemanager.ui.property.update

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent

sealed class PropertyUpdateIntent : MviIntent {
    data class InitialIntent(val propertyId: String) : PropertyUpdateIntent()
    data class UpdatePropertyIntent(val property: Property) : PropertyUpdateIntent()
}
