package com.openclassrooms.realestatemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.*

@Database(entities = [Property::class], version = 1, exportSchema = false)
@TypeConverters(*arrayOf(PropertyTypeConverter::class,
        PropertyStatusConverter::class,
        PictureTypeConverter::class,
        InterestPointConverter::class,
        DateConverter::class
))
abstract class AppDatabase : RoomDatabase() {

    abstract fun propertyDao(): PropertyDao

    companion object {
        const val DATABASE_NAME: String = "real_estate_manger.db"
    }
}