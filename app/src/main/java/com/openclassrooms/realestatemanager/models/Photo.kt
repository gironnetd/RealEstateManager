package com.openclassrooms.realestatemanager.models

import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Photo.Companion.TABLE_NAME
import com.openclassrooms.realestatemanager.util.Constants.GS_REFERENCE_PREFIX
import com.openclassrooms.realestatemanager.util.Constants.MAIN_FILE_NAME
import com.openclassrooms.realestatemanager.util.Constants.PHOTOS_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.SLASH
import com.openclassrooms.realestatemanager.util.Constants.THUMBNAIL_FILE_NAME
import kotlinx.parcelize.Parcelize
import java.io.File

fun Photo.storageLocalDatabase(cacheDir: File, isThumbnail: Boolean = false): String {
    val destination = StringBuilder()
    destination.append(cacheDir.absolutePath)
        .append(SLASH)
        .append(PROPERTIES_COLLECTION)
        .append(SLASH)
        .append(propertyId)
        .append(SLASH)
        .append(PHOTOS_COLLECTION)
        .append(SLASH)
        .append(id)
        .append(SLASH)

    val root = File(destination.toString())

    if(!root.exists()) {
        root.mkdirs()
    }

    if (isThumbnail) destination.append(THUMBNAIL_FILE_NAME) else destination.append(MAIN_FILE_NAME)
    return destination.toString()
}

fun Photo.storageUrl(storageBucket: String, isThumbnail: Boolean = false): String {
    val url = StringBuilder()
    url.append(GS_REFERENCE_PREFIX)
        .append(storageBucket)
        .append(SLASH)
        .append(PROPERTIES_COLLECTION)
        .append(SLASH)
        .append(propertyId)
        .append(SLASH)
        .append(PHOTOS_COLLECTION)
        .append(SLASH)
        .append(id)
        .append(SLASH)

    if (isThumbnail) url.append(THUMBNAIL_FILE_NAME) else url.append(MAIN_FILE_NAME)
    return url.toString()
}

@Parcelize
@Entity(tableName = TABLE_NAME, primaryKeys = [COLUMN_ID, "property_id"])
data class Photo (
    @SerializedName(value = "id")
    @ColumnInfo(index = true, name = COLUMN_ID)
    var id: String = "",

    @ColumnInfo(name = "property_id")
    @get:Exclude var propertyId: String = "",
    var description: String = "",

    @ColumnInfo(name = "main_photo")
    var mainPhoto: Boolean = false,

    @SerializedName(value = "type")
    @ColumnInfo(name = "type")
    var type: PhotoType = PhotoType.NONE,

    @Ignore
    @get:Exclude
    var bitmap: Bitmap? = null

) : Parcelable {
    constructor(cursor: Cursor/*, isMainPhoto: Boolean = false*/): this() {
        //mainPhoto = isMainPhoto
//        if(isMainPhoto) {
//            id = cursor.getString(
//                cursor.getColumnIndex(
//                    PREFIX_MAIN_PHOTO + COLUMN_ID))
//            propertyId = cursor.getString(
//                cursor.getColumnIndex(
//                    PREFIX_MAIN_PHOTO + COLUMN_PHOTO_PROPERTY_ID
//                ))
//            description = cursor.getString(
//                cursor.getColumnIndex(
//                    PREFIX_MAIN_PHOTO + COLUMN_PHOTO_DESCRIPTION
//                ))
//            type = PhotoType.valueOf(
//                cursor.getString(
//                    cursor.getColumnIndex(
//                        PREFIX_MAIN_PHOTO + COLUMN_PHOTO_TYPE
//                    )))
//            mainPhoto = isMainPhoto
//        } else {
            id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
            propertyId = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PROPERTY_ID))
            description = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_DESCRIPTION))
            type = PhotoType.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_TYPE)))

            mainPhoto = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MAIN_PHOTO)) == 1
    //    }
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

//    constructor(string: String): this() {
//        val split = string.split(SEPARATOR)
//        id = split[0]
//        propertyId = split[1]
//        description = split[2]
//        type = PhotoType.valueOf(split[3])
//        //mainPhoto = split[4].toBoolean()
//    }

    companion object {

        const val SEPARATOR = ","
        /** The name of the Photo table.  */
        const val TABLE_NAME: String = "photos"

        /** The name of the ID column.  */
        const val COLUMN_ID: String = BaseColumns._ID

        /** The name of the property id column.  */
        const val COLUMN_PHOTO_PROPERTY_ID = "property_id"

        /** The name of the description column.  */
        const val COLUMN_PHOTO_DESCRIPTION = "description"

        /** The name of the type column.  */
        const val COLUMN_PHOTO_TYPE = "type"

        const val COLUMN_IS_MAIN_PHOTO = "main_photo"

        /** The name of the prefix main photo in database.  */
        const val PREFIX_MAIN_PHOTO = "main_photo_"

        @NonNull
        fun fromContentValues(values: ContentValues?): Photo {
            val photo = Photo()
            values?.let {
                if ( it.containsKey(COLUMN_ID)) {
                    photo.id = it.getAsString(Property.COLUMN_ID)
                }

                if ( it.containsKey(COLUMN_PHOTO_PROPERTY_ID)) {
                    photo.propertyId = it.getAsString(COLUMN_PHOTO_PROPERTY_ID)
                }

                if ( it.containsKey(COLUMN_PHOTO_DESCRIPTION)) {
                    photo.description = it.getAsString(COLUMN_PHOTO_DESCRIPTION)
                }

                if (it.containsKey(COLUMN_PHOTO_TYPE)) {
                    photo.type = PhotoType.valueOf(it.getAsString(COLUMN_PHOTO_TYPE))
                }

                if(it.containsKey(COLUMN_IS_MAIN_PHOTO)) {
                    photo.mainPhoto = it.getAsBoolean(COLUMN_IS_MAIN_PHOTO)
                }
            }
            return photo
        }
    }
}


