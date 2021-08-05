package com.openclassrooms.realestatemanager.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.data.cache.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoDataSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoStorageSource
import com.openclassrooms.realestatemanager.data.fake.property.FakePropertyDataSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoDataSource
import com.openclassrooms.realestatemanager.data.source.photo.PhotoStorageSource
import com.openclassrooms.realestatemanager.data.source.property.PropertyDataSource
import com.openclassrooms.realestatemanager.util.*
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.ImmediateSchedulerProvider
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
    fun provideStorage(): FirebaseStorage {
        val storage = FirebaseStorage.getInstance(ConstantsTest.FIREBASE_STORAGE_DEFAULT_BUCKET)
        storage.useEmulator(ConstantsTest.FIREBASE_EMULATOR_HOST,
            ConstantsTest.FIREBASE_STORAGE_PORT)
        return storage
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDb(): AppDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build()

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyDao(db: AppDatabase): PropertyDao = db.propertyDao()

    @JvmStatic
    @Singleton
    @Provides
    fun provideGlideRequestManager(): GlideManager = FakeGlideRequestManager()

    @JvmStatic
    @Singleton
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider = ImmediateSchedulerProvider()

    @JvmStatic
    @Singleton
    @Provides
    fun provideUiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @JvmStatic
    @Singleton
    @Provides
    fun provideJsonUtil(): JsonUtil = JsonUtil()

    @JvmStatic
    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application

    @JvmStatic
    @Singleton
    @Provides
    fun provideNetworkConnectionLiveData(context: Context): LiveData<Boolean> =
        NetworkConnectionLiveData(context = context)

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyDataSource(jsonUtil: JsonUtil): PropertyDataSource {
        return FakePropertyDataSource(jsonUtil = jsonUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheDataSource(jsonUtil: JsonUtil): PhotoDataSource {
        return FakePhotoDataSource(jsonUtil = jsonUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheStorageSource(jsonUtil: JsonUtil, context: Context): PhotoStorageSource {
        return FakePhotoStorageSource(jsonUtil = jsonUtil, cacheDir = context.cacheDir)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheSource(cacheDataSource: PhotoDataSource,
                                cacheStorageSource: PhotoStorageSource
    ): PhotoCacheSource {
        return PhotoCacheSource(cacheData = cacheDataSource,
            cacheStorage = cacheStorageSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyCacheSource(cacheDataSource: PropertyDataSource): PropertyCacheSource {
        return PropertyCacheSource(cacheData = cacheDataSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyRemoteSource(remoteDataSource: PropertyDataSource): PropertyRemoteSource {
        return PropertyRemoteSource(remoteData = remoteDataSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoRemoteSource(remoteDataSource: PhotoDataSource,
                                 remoteStorageSource: PhotoStorageSource
    ): PhotoRemoteSource {
        return PhotoRemoteSource(remoteData = remoteDataSource, remoteStorage = remoteStorageSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRemoteDataSource(propertyRemoteSource: PropertyRemoteSource,
                                photoRemoteSource: PhotoRemoteSource
    ): DataSource<PropertyRemoteSource, PhotoRemoteSource> {
        return DataSource(propertySource = propertyRemoteSource, photoSource = photoRemoteSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideCacheDataSource(propertyCacheSource: PropertyCacheSource,
                               photoCacheSource: PhotoCacheSource
    ): DataSource<PropertyCacheSource, PhotoCacheSource> {
        return DataSource(propertySource = propertyCacheSource, photoSource = photoCacheSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyRepository(networkConnectionLiveData: NetworkConnectionLiveData,
                                  remoteDataSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>,
                                  cacheDataSource: DataSource<PropertyCacheSource, PhotoCacheSource>
    ): PropertyRepository = DefaultPropertyRepository(
        networkConnectionLiveData = networkConnectionLiveData,
        remoteDataSource = remoteDataSource,
        cacheDataSource = cacheDataSource
    )
}