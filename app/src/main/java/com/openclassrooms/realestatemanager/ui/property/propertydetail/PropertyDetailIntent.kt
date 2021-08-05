package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent

sealed class PropertyDetailIntent : MviIntent {
    data class InitialIntent(val propertyId: String) : PropertyDetailIntent()
    data class PopulatePropertyIntent(val propertyId: String) : PropertyDetailIntent()
}