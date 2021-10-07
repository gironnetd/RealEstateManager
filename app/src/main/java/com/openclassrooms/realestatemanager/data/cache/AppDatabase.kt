package com.openclassrooms.realestatemanager.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openclassrooms.realestatemanager.data.cache.dao.PhotoDao
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.property.*

@Database(entities = [Property::class, Photo::class], version = 1, exportSchema = false)
@TypeConverters(*arrayOf(
    PropertyTypeConverter::class,
        PropertyStatusConverter::class,
        PhotoTypeConverter::class,
        InterestPointConverter::class,
        DateConverter::class
))
abstract class AppDatabase : RoomDatabase() {

    abstract fun propertyDao(): PropertyDao
    abstract fun photoDao(): PhotoDao

    companion object {
        const val DATABASE_NAME: String = "real_estate_manger.db"
    }
}