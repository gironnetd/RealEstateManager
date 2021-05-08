package com.openclassrooms.realestatemanager.models

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Picture.Companion.TABLE_NAME
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

@Entity(tableName = TABLE_NAME, primaryKeys = [COLUMN_ID, "property_id"])
data class Picture(
        @SerializedName(value = "id")
        @ColumnInfo(index = true, name = COLUMN_ID)
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
                            PREFIX_MAIN_PICTURE + COLUMN_ID))
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
            id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
            propertyId = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE_PROPERTY_ID))
            description = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE_DESCRIPTION))
            type = PictureType.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE_TYPE)))
        }
    }

    override fun toString(): String {
        return StringBuffer()
                .append(id)
                .append(SEPARATOR)
                .append(propertyId)
                .append(SEPARATOR)
                .append(description)
                .append(SEPARATOR)
                .append(type)
                .toString()
    }

    constructor(string: String): this() {
        val split = string.split(SEPARATOR)
        id = split[0]
        propertyId = split[1]
        description = split[2]
        type = PictureType.valueOf(split[3])
    }

    companion object {

        const val SEPARATOR = ","
        /** The name of the Picture table.  */
        const val TABLE_NAME: String = "pictures"

        /** The name of the ID column.  */
        const val COLUMN_ID: String = BaseColumns._ID

        /** The name of the property id column.  */
        const val COLUMN_PICTURE_PROPERTY_ID = "property_id"

        /** The name of the description column.  */
        const val COLUMN_PICTURE_DESCRIPTION = "description"

        /** The name of the type column.  */
        const val COLUMN_PICTURE_TYPE = "type"

        /** The name of the prefix main picture in database.  */
        const val PREFIX_MAIN_PICTURE = "main_picture_"

        @NonNull
        fun fromContentValues(values: ContentValues?): Picture {
            val picture = Picture()
            values?.let {

                if ( it.containsKey(COLUMN_ID)) {
                    picture.id = it.getAsString(Property.COLUMN_ID)
                }

                if ( it.containsKey(COLUMN_PICTURE_PROPERTY_ID)) {
                    picture.propertyId = it.getAsString(COLUMN_PICTURE_PROPERTY_ID)
                }

                if ( it.containsKey(COLUMN_PICTURE_DESCRIPTION)) {
                    picture.description = it.getAsString(COLUMN_PICTURE_DESCRIPTION)
                }

                if (it.containsKey(COLUMN_PICTURE_TYPE)) {
                    picture.type = PictureType.valueOf(it.getAsString(COLUMN_PICTURE_TYPE))
                }
            }
            return picture
        }
    }
}


