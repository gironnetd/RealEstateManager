package com.openclassrooms.realestatemanager.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.remote.DefaultPropertyApiService
import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.util.JsonUtil
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object TestAppModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreSettings(): FirebaseFirestoreSettings
            = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestore(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
        // the host computer from an Android emulator.
        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = settings
        return firestore
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyApiService(firestore: FirebaseFirestore): PropertyApiService
            = DefaultPropertyApiService(firestore = firestore)

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
    fun providePropertyDao(db: AppDatabase): PropertyDao {
        return db.propertyDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideJsonUtil(application: Application): JsonUtil {
        return JsonUtil(application)
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