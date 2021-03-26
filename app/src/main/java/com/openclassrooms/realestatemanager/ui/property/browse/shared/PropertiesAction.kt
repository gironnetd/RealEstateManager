package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseAction

sealed class PropertiesAction : BaseAction {

    object LoadProperties : PropertiesAction()
}
