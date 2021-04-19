package com.openclassrooms.realestatemanager.data.local.dao

import android.database.Cursor
import androidx.room.*
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_PICTURE_PROPERTY_ID
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Property.Companion.TABLE_NAME
import com.openclassrooms.realestatemanager.models.Property.Companion.cursorFromProperties
import io.reactivex.Single

@Dao
abstract class PropertyDao {

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    abstract fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePropertyAlone(property: Property): Long

    fun saveProperty(property: Property): Long {
        savePictures(property.pictures)
        return  savePropertyAlone(property)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun savePropertiesAlone(properties: List<Property>): LongArray

    fun saveProperties(properties: List<Property>): LongArray {
        properties.forEach { property ->
            savePictures(property.pictures)
        }
        return savePropertiesAlone(properties)
    }

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    abstract fun findPropertyById(id: Long): Cursor

    @Query("SELECT * FROM $TABLE_NAME ORDER BY _id ASC")
    abstract fun findAllPropertiesAlone(): Cursor

    fun findAllProperties(): Cursor {
        val allProperties = findAllPropertiesAlone().toList { Property(it) }
        allProperties.forEach { property ->
            var picturesByPropertyId = findPicturesByPropertyId(propertyId = property.id)
                    .toList { Picture(it, false) }
            property.pictures = picturesByPropertyId.toMutableList()
        }
        return cursorFromProperties(allProperties)
    }

    @Delete
    abstract fun deleteAllProperties(properties: List<Property>): Single<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun savePicture(picture: Picture): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun savePictures(pictures: List<Picture>): LongArray

    @Query("SELECT * FROM ${Picture.TABLE_NAME} WHERE $COLUMN_PICTURE_PROPERTY_ID = :propertyId")
    abstract fun findPicturesByPropertyId(propertyId: String): Cursor
}