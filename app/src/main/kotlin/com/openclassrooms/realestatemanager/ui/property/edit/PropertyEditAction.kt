package com.openclassrooms.realestatemanager.ui.property.edit

import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.mvibase.MviAction

sealed class PropertyEditAction : MviAction {

    sealed class PopulatePropertyAction : PropertyEditAction() {
        data class PopulatePropertyAction(val propertyId: String) : PropertyEditAction.PopulatePropertyAction()
    }

    sealed class UpdatePropertyAction : PropertyEditAction() {
        data class UpdateAction(val property: Property) : PropertyEditAction.UpdatePropertyAction()
    }

    sealed class CreatePropertyAction : PropertyEditAction() {
        data class CreateAction(val property: Property) : PropertyEditAction.CreatePropertyAction()
    }
}
