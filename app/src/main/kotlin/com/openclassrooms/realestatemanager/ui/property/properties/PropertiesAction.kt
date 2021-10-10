package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.ui.mvibase.MviAction

sealed class PropertiesAction : MviAction {
    object LoadPropertiesAction : PropertiesAction()
}
