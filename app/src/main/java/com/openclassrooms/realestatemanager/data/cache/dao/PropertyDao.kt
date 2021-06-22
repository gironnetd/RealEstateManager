package com.openclassrooms.realestatemanager.data.cache.dao

import android.database.Cursor
import androidx.room.*
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Property.Companion.TABLE_NAME
import io.reactivex.Single

@Dao
interface PropertyDao {

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProperty(property: Property): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProperties(vararg properties: Property): LongArray

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPropertyById(id: Long): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPropertyById(id: String): Cursor

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID IN (:ids)")
    fun findPropertiesByIds(ids: List<String>): List<Property>

    @Query("SELECT * FROM $TABLE_NAME ORDER BY _id ASC")
    fun findAllProperties(): Cursor

    @Delete
    fun deleteProperties(vararg properties: Property): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID IN (:ids)")
    fun deletePropertiesByIds(ids: List<String>): Int

    @Delete
    fun deleteAllProperties(properties: List<Property>): Single<Int>

    @Query("DELETE FROM $TABLE_NAME")
    fun deleteAllProperties(): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun deletePropertyById(id: Long): Int

    @Query("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun deletePropertyById(id: String): Int

    @Update
    fun updateProperty(property: Property): Int

    @Update
    fun updateProperties(vararg properties: Property): Int
}