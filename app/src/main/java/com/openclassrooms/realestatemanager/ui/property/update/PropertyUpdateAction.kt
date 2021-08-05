package com.openclassrooms.realestatemanager.ui.property.update

import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviAction

sealed class PropertyUpdateAction : MviAction {
    data class PopulatePropertyAction(val propertyId: String): PropertyUpdateAction()
    data class UpdatePropertyAction(val property: Property) : PropertyUpdateAction()
}
