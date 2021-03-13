package com.openclassrooms.realestatemanager.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.AppDatabase.Companion.DATABASE_NAME
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.remote.DefaultPropertyApiService
import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
object AppModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestore() = Firebase.firestore

    @JvmStatic
    @Singleton
    @Named("defaultPropertyApiService")
    @Provides
    fun providePropertyApiService(firestore: FirebaseFirestore): PropertyApiService
            = DefaultPropertyApiService(firestore = firestore)

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

    @JvmStatic
    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        return application
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNetworkConnectionLiveData(context: Context): LiveData<Boolean> {
        return NetworkConnectionLiveData(context = context)
    }
}