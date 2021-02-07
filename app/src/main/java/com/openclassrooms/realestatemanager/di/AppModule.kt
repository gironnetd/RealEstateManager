package com.openclassrooms.realestatemanager.di

import android.app.Application
import androidx.room.Room
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.AppDatabase.Companion.DATABASE_NAME
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
                .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration() // get correct db version if schema changed
                .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyDao(db: AppDatabase): PropertyDao {
        return db.propertyDao()
    }
}