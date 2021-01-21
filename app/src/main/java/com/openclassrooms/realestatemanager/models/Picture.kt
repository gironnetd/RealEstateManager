package com.openclassrooms.realestatemanager.models

import com.google.firebase.firestore.Exclude
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.Constants.GS_REFERENCE
import com.openclassrooms.realestatemanager.util.Constants.MAIN_FILE_NAME
import com.openclassrooms.realestatemanager.util.Constants.PICTURES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.THUMBNAIL_FILE_NAME

data class Picture(
        var id: String = "",
        @get:Exclude var propertyId: String = "",
        val description: String = "",
        val type: PictureType = PictureType.NONE,
)

fun Picture.storageUrl(isThumbnail: Boolean = false): String {
    var url = StringBuilder()
    url.append(GS_REFERENCE)
            .append(PROPERTIES_COLLECTION)
            .append(Constants.SLASH)
            .append(propertyId)
            .append(Constants.SLASH)
            .append(PICTURES_COLLECTION)
            .append(Constants.SLASH)
            .append(id)
            .append(Constants.SLASH)

    if (isThumbnail) url.append(THUMBNAIL_FILE_NAME) else url.append(MAIN_FILE_NAME)
    return url.toString()
}



