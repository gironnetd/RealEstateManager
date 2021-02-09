package com.openclassrooms.realestatemanager.data.local.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Property.Companion.TABLE_NAME

@Dao
interface PropertyDao {

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProperty(property: Property): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProperties(properties: List<Property>): LongArray

    @Query("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = :id")
    fun findPropertyById(id: Long): Cursor

    @Query("SELECT * FROM $TABLE_NAME")
    fun findAll(): Cursor

    @Query("SELECT * FROM $TABLE_NAME")
    fun findAllProperties(): List<Property>


}