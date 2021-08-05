package com.openclassrooms.realestatemanager.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.data.cache.AppDatabase.Companion.DATABASE_NAME
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDao
import com.openclassrooms.realestatemanager.util.GlideManager
import com.openclassrooms.realestatemanager.util.GlideRequestManager
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestore() = Firebase.firestore

    @JvmStatic
    @Singleton
    @Provides
    fun provideStorage() = Firebase.storage

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
    fun providePropertyDao(db: AppDatabase): PropertyDao = db.propertyDao()

    @JvmStatic
    @Singleton
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider

    @JvmStatic
    @Singleton
    @Provides
    fun provideGlideRequestManager(application: Application ): GlideManager =
         GlideRequestManager(Glide.with(application))

    @JvmStatic
    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application

    @JvmStatic
    @Singleton
    @Provides
    fun provideNetworkConnectionLiveData(context: Context): LiveData<Boolean> =
         NetworkConnectionLiveData(context = context)
}