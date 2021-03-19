package com.openclassrooms.realestatemanager.ui.property.browse.properties

import com.openclassrooms.realestatemanager.base.BaseIntent

sealed class PropertiesIntent : BaseIntent {

    object InitialIntent : PropertiesIntent()

    object LoadPropertiesIntent : PropertiesIntent()
}
