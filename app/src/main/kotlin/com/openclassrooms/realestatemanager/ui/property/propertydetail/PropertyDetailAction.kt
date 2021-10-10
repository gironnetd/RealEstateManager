package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.openclassrooms.realestatemanager.ui.mvibase.MviAction

sealed class PropertyDetailAction : MviAction {
    data class PopulatePropertyAction(val propertyId: String): PropertyDetailAction()
}
