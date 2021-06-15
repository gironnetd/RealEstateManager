package com.openclassrooms.realestatemanager.data.local.dao

import android.database.Cursor
import androidx.room.*
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_PHOTO_PROPERTY_ID
import com.openclassrooms.realestatemanager.models.Photo.Companion.TABLE_NAME

@Dao
interface PhotoDao {

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_PHOTO_PROPERTY_ID = :propertyId")
    fun count(propertyId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePhoto(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePhotos(vararg  photos: Photo): LongArray

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_PHOTO_PROPERTY_ID = :propertyId")
    fun findPhotosByPropertyId(propertyId: String): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPhotoById(id: Long): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPhotoById(id: String): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID IN (:ids)")
    fun findPhotosByIds(ids: List<String>): List<Photo>

    @Query("SELECT * FROM $TABLE_NAME ORDER BY _id ASC")
    fun findAllPhotos(): Cursor

    @Delete
    fun deletePhotos(vararg photos: Photo): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID IN (:ids)")
    fun deletePhotosByIds(ids: List<String>): Int

    @Query("DELETE FROM $TABLE_NAME")
    fun deleteAllPhotos(): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun deletePhotoById(id: Long): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun deletePhotoById(id: String): Int

    @Update
    fun updatePhoto(photo: Photo): Int

    @Update
    fun updatePhotos(vararg photo: Photo): Int
}