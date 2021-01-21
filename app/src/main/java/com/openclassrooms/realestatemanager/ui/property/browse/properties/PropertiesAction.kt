package com.openclassrooms.realestatemanager.ui.property.browse.properties

import com.openclassrooms.realestatemanager.base.BaseAction

sealed class PropertiesAction : BaseAction {

    object LoadProperties : PropertiesAction()
}
