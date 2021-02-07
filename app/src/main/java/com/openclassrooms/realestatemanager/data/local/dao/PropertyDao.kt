package com.openclassrooms.realestatemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openclassrooms.realestatemanager.models.Property

@Dao
interface PropertyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProperty(property: Property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProperties(vararg properties: Property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProperties(properties: List<Property>)

    @Query("SELECT * FROM properties")
    fun findAllProperties(): List<Property>
}