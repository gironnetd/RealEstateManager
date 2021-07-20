package com.openclassrooms.realestatemanager.di.property.browse

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.data.cache.data.PhotoCacheDataSource
import com.openclassrooms.realestatemanager.data.cache.data.PropertyCacheDataSource
import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.cache.storage.PhotoCacheStorageSource
import com.openclassrooms.realestatemanager.data.remote.data.PhotoRemoteDataSource
import com.openclassrooms.realestatemanager.data.remote.data.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.remote.storage.PhotoRemoteStorageSource
import com.openclassrooms.realestatemanager.data.repository.property.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepository
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
object BrowseModule {

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoCacheDataSource(db: AppDatabase): PhotoCacheDataSource {
        return PhotoCacheDataSource(photoDao = db.photoDao())
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoCacheStorageSource(context: Context): PhotoCacheStorageSource {
        return PhotoCacheStorageSource(cacheDir = context.cacheDir)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoCacheSource(cacheDataSource: PhotoCacheDataSource,
                                cacheStorageSource: PhotoCacheStorageSource
    ): PhotoCacheSource {
        return PhotoCacheSource(cacheData = cacheDataSource,
            cacheStorage = cacheStorageSource
        )
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoRemoteDataSource(firestore: FirebaseFirestore): PhotoRemoteDataSource {
        return PhotoRemoteDataSource(firestore = firestore)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoRemoteStorageSource(storage: FirebaseStorage): PhotoRemoteStorageSource {
        return PhotoRemoteStorageSource(storage = storage)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePhotoRemoteSource(remoteDataSource: PhotoRemoteDataSource, remoteStorageSource: PhotoRemoteStorageSource): PhotoRemoteSource {
        return PhotoRemoteSource(remoteData = remoteDataSource, remoteStorage = remoteStorageSource)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyCacheDataSource(db: AppDatabase): PropertyCacheDataSource {
        return PropertyCacheDataSource(propertyDao = db.propertyDao())
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyCacheSource(cacheDataSource: PropertyCacheDataSource): PropertyCacheSource {
        return PropertyCacheSource(cacheData = cacheDataSource)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyRemoteDataSource(firestore: FirebaseFirestore): PropertyRemoteDataSource {
        return PropertyRemoteDataSource(firestore = firestore)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyRemoteSource(remoteDataSource: PropertyRemoteDataSource): PropertyRemoteSource {
        return PropertyRemoteSource(remoteData = remoteDataSource)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideRemoteDataSource(propertyRemoteSource: PropertyRemoteSource,
                                photoRemoteSource: PhotoRemoteSource
    ): DataSource<PropertyRemoteSource, PhotoRemoteSource> {
        return DataSource(propertySource = propertyRemoteSource, photoSource = photoRemoteSource)
    }

    @JvmStatic
    @BrowseScope
    @Provides
    fun provideCacheDataSource(propertyCacheSource: PropertyCacheSource,
                                photoCacheSource: PhotoCacheSource
    ): DataSource<PropertyCacheSource, PhotoCacheSource> {
        return DataSource(propertySource = propertyCacheSource, photoSource = photoCacheSource)
    }

    @JvmStatic
    @BrowseScope
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