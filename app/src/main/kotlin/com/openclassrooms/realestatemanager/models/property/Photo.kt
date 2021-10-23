package com.openclassrooms.realestatemanager.models.property

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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.models.property.Photo.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.property.Photo.Companion.TABLE_NAME
import com.openclassrooms.realestatemanager.util.BitmapUtil.sameAs
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

    if (!root.exists()) {
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
data class Photo(
    @SerializedName(value = "id")
    @ColumnInfo(index = true, name = COLUMN_ID)
    var id: String = Firebase.firestore.collection(PHOTOS_COLLECTION).document().id,

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
    var bitmap: Bitmap? = null,

    @ColumnInfo(name = "locally_updated")
    @get:Exclude
    var locallyUpdated: Boolean = false,

    @ColumnInfo(name = "locally_created")
    @get:Exclude
    var locallyCreated: Boolean = false,

    @ColumnInfo(name = "locally_deleted")
    @get:Exclude
    var locallyDeleted: Boolean = false
) : Parcelable {

    constructor(cursor: Cursor) : this() {
        id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
        propertyId = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PROPERTY_ID))
        description = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_DESCRIPTION))
        type = PhotoType.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_TYPE)))
        mainPhoto = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MAIN_PHOTO)) == 1
        locallyUpdated = cursor.getInt(cursor.getColumnIndex(COLUMN_LOCALLY_UPDATED)) == 1
        locallyCreated = cursor.getInt(cursor.getColumnIndex(COLUMN_LOCALLY_CREATED)) == 1
    }

    fun deepCopy(
        id: String = this.id, propertyId: String = this.propertyId,
        description: String = this.description, mainPhoto: Boolean = this.mainPhoto,
        type: PhotoType = this.type, bitmap: Bitmap? = this.bitmap,
        locallyUpdated: Boolean = this.locallyUpdated,
        locallyCreated: Boolean = this.locallyCreated,
        locallyDeleted: Boolean = this.locallyDeleted
    ): Photo = Photo(
        id, propertyId, description, mainPhoto, type,
        bitmap, locallyUpdated, locallyCreated, locallyDeleted
    )

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photo

        if (id != other.id) return false
        if (propertyId != other.propertyId) return false
        if (description != other.description) return false
        if (mainPhoto != other.mainPhoto) return false
        if (type != other.type) return false
        if (bitmap != null && other.bitmap != null) if (!sameAs(bitmap!!, other.bitmap!!)) return false
        if (locallyUpdated != other.locallyUpdated) return false
        if (locallyCreated != other.locallyCreated) return false
        if (locallyDeleted != other.locallyDeleted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + propertyId.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + mainPhoto.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        result = 31 * result + locallyUpdated.hashCode()
        result = 31 * result + locallyCreated.hashCode()
        result = 31 * result + locallyDeleted.hashCode()
        return result
    }

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

        /** The name of the main photo column.  */
        const val COLUMN_IS_MAIN_PHOTO = "main_photo"

        /** The name of the locally updated column.  */
        const val COLUMN_LOCALLY_UPDATED = "locally_updated"

        /** The name of the locally created column.  */
        const val COLUMN_LOCALLY_CREATED = "locally_created"

        @NonNull
        fun fromContentValues(values: ContentValues?): Photo {
            val photo = Photo()
            values?.let {
                if (it.containsKey(COLUMN_ID)) {
                    photo.id = it.getAsString(Property.COLUMN_ID)
                }

                if (it.containsKey(COLUMN_PHOTO_PROPERTY_ID)) {
                    photo.propertyId = it.getAsString(COLUMN_PHOTO_PROPERTY_ID)
                }

                if (it.containsKey(COLUMN_PHOTO_DESCRIPTION)) {
                    photo.description = it.getAsString(COLUMN_PHOTO_DESCRIPTION)
                }

                if (it.containsKey(COLUMN_PHOTO_TYPE)) {
                    photo.type = PhotoType.valueOf(it.getAsString(COLUMN_PHOTO_TYPE))
                }

                if (it.containsKey(COLUMN_IS_MAIN_PHOTO)) {
                    photo.mainPhoto = it.getAsBoolean(COLUMN_IS_MAIN_PHOTO)
                }

                if (it.containsKey(COLUMN_LOCALLY_UPDATED)) {
                    photo.locallyUpdated = it.getAsBoolean(COLUMN_LOCALLY_UPDATED)
                }

                if (it.containsKey(COLUMN_LOCALLY_CREATED)) {
                    photo.locallyCreated = it.getAsBoolean(COLUMN_LOCALLY_CREATED)
                }
            }
            return photo
        }
    }
}
