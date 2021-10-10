package com.openclassrooms.realestatemanager.ui.property.edit

import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent

sealed class PropertyEditIntent : MviIntent {

    sealed class PropertyUpdateIntent : PropertyEditIntent() {
        data class InitialIntent(val propertyId: String) : PropertyUpdateIntent()
        data class UpdatePropertyIntent(val property: Property) : PropertyUpdateIntent()
    }

    sealed class PropertyCreateIntent : PropertyEditIntent() {
        object InitialIntent : PropertyCreateIntent()
        data class CreatePropertyIntent(val property: Property) : PropertyCreateIntent()
    }
}
