package com.openclassrooms.realestatemanager.di

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.util.JsonUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object TestAppModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideJsonUtil(application: Application): JsonUtil {
        return JsonUtil(application)
    }
}