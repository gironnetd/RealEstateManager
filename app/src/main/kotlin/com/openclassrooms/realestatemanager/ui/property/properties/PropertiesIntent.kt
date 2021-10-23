package com.openclassrooms.realestatemanager.ui.property.properties

import com.openclassrooms.realestatemanager.ui.mvibase.MviIntent

sealed class PropertiesIntent : MviIntent {
    object InitialIntent : PropertiesIntent()
    object LoadPropertiesIntent : PropertiesIntent()
}
