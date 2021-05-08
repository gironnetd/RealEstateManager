package com.openclassrooms.realestatemanager.data.local.dao

import android.database.Cursor
import androidx.room.*
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_PICTURE_PROPERTY_ID
import com.openclassrooms.realestatemanager.models.Picture.Companion.TABLE_NAME
import io.reactivex.Single

@Dao
interface PictureDao {

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePicture(picture: Picture): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePictures(pictures: List<Picture>): LongArray

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_PICTURE_PROPERTY_ID = :propertyId")
    fun findPicturesByPropertyId(propertyId: String): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPictureById(id: Long): Cursor

    @Query("SELECT * FROM ${TABLE_NAME} ORDER BY _id ASC")
    fun findAllPictures(): Cursor

    @Delete
    fun deleteAllPictures(properties: List<Picture>): Single<Int>

    @Query("DELETE FROM ${TABLE_NAME}")
    fun deleteAllPictures(): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun deleteById(id: Long): Int

    @Update
    fun updatePicture(picture: Picture): Int
}