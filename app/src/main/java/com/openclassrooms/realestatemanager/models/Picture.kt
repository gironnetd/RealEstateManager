package com.openclassrooms.realestatemanager.models

import android.database.Cursor
import androidx.room.ColumnInfo
import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.Constants.GS_REFERENCE
import com.openclassrooms.realestatemanager.util.Constants.MAIN_FILE_NAME
import com.openclassrooms.realestatemanager.util.Constants.PICTURES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.THUMBNAIL_FILE_NAME

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

data class Picture(
        @SerializedName(value = "id")
        @ColumnInfo(name = "id")
        var id: String = "",

        @ColumnInfo(name = "property_id")
        @get:Exclude var propertyId: String = "",
        var description: String = "",
        @SerializedName(value = "type")
        @ColumnInfo(name = "type")
        var type: PictureType = PictureType.NONE,
) {
    constructor(cursor: Cursor, isMainPicture: Boolean = false): this() {
        if(isMainPicture) {
            id = cursor.getString(
                    cursor.getColumnIndex(
                            PREFIX_MAIN_PICTURE + COLUMN_PICTURE_ID))
            propertyId = cursor.getString(
                    cursor.getColumnIndex(
                            PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID
                            ))
            description = cursor.getString(
                    cursor.getColumnIndex(
                        PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION
                    ))
            type = PictureType.valueOf(
                    cursor.getString(
                            cursor.getColumnIndex(
                                    PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE
                            )))
        } else {
            id = cursor.getString(
                    cursor.getColumnIndex(
                            COLUMN_PICTURE_ID))
            propertyId = cursor.getString(
                    cursor.getColumnIndex(
                            COLUMN_PICTURE_PROPERTY_ID
                    ))
            description = cursor.getString(
                    cursor.getColumnIndex(
                            COLUMN_PICTURE_DESCRIPTION
                    ))
            type = PictureType.valueOf(
                    cursor.getString(
                            cursor.getColumnIndex(
                                    COLUMN_PICTURE_TYPE
                            )))
        }
    }

    companion object {
        /** The name of the id column.  */
        const val COLUMN_PICTURE_ID = "id"

        /** The name of the property id column.  */
        const val COLUMN_PICTURE_PROPERTY_ID = "property_id"

        /** The name of the description column.  */
        const val COLUMN_PICTURE_DESCRIPTION = "description"

        /** The name of the type column.  */
        const val COLUMN_PICTURE_TYPE = "type"

        /** The name of the prefix main picture in database.  */
        const val PREFIX_MAIN_PICTURE = "main_picture_"
    }
}


