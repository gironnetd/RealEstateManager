package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseAction
import com.openclassrooms.realestatemanager.models.Property

sealed class PropertiesAction : BaseAction {

    object LoadPropertiesAction : PropertiesAction()

    data class UpdatePropertyAction(val property: Property) : PropertiesAction()
}
