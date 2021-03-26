package com.openclassrooms.realestatemanager.ui.property.browse.shared

import com.openclassrooms.realestatemanager.base.BaseResult
import com.openclassrooms.realestatemanager.base.model.TaskStatus
import com.openclassrooms.realestatemanager.models.Property

sealed class PropertiesResult : BaseResult {

    class LoadPropertiesTask(
            val status: TaskStatus,
            val properties: List<Property>? = null,
    )
        : PropertiesResult() {

        companion object {

            internal fun success(properties: List<Property>?): LoadPropertiesTask {
                return LoadPropertiesTask(TaskStatus.SUCCESS, properties)
            }

            internal fun failure(): LoadPropertiesTask {
                return LoadPropertiesTask(TaskStatus.FAILURE, null)
            }

            internal fun inFlight(): LoadPropertiesTask {
                return LoadPropertiesTask(TaskStatus.IN_FLIGHT)
            }
        }
    }
}
