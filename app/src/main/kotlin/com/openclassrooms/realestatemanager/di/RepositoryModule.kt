package com.openclassrooms.realestatemanager.di

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
import com.openclassrooms.realestatemanager.data.repository.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RepositoryModule {

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheDataSource(db: AppDatabase): PhotoCacheDataSource {
        return PhotoCacheDataSource(photoDao = db.photoDao())
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheStorageSource(context: Context): PhotoCacheStorageSource {
        return PhotoCacheStorageSource(cacheDir = context.cacheDir)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoCacheSource(
        cacheDataSource: PhotoCacheDataSource,
        cacheStorageSource: PhotoCacheStorageSource
    ): PhotoCacheSource {
        return PhotoCacheSource(
            cacheData = cacheDataSource,
            cacheStorage = cacheStorageSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoRemoteDataSource(firestore: FirebaseFirestore): PhotoRemoteDataSource {
        return PhotoRemoteDataSource(firestore = firestore)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoRemoteStorageSource(storage: FirebaseStorage): PhotoRemoteStorageSource {
        return PhotoRemoteStorageSource(storage = storage)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePhotoRemoteSource(
        remoteDataSource: PhotoRemoteDataSource,
        remoteStorageSource: PhotoRemoteStorageSource
    ): PhotoRemoteSource {
        return PhotoRemoteSource(remoteData = remoteDataSource, remoteStorage = remoteStorageSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyCacheDataSource(db: AppDatabase): PropertyCacheDataSource {
        return PropertyCacheDataSource(propertyDao = db.propertyDao())
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyCacheSource(cacheDataSource: PropertyCacheDataSource): PropertyCacheSource {
        return PropertyCacheSource(cacheData = cacheDataSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyRemoteDataSource(firestore: FirebaseFirestore): PropertyRemoteDataSource {
        return PropertyRemoteDataSource(firestore = firestore)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyRemoteSource(remoteDataSource: PropertyRemoteDataSource): PropertyRemoteSource {
        return PropertyRemoteSource(remoteData = remoteDataSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRemoteDataSource(
        propertyRemoteSource: PropertyRemoteSource,
        photoRemoteSource: PhotoRemoteSource
    ): DataSource<PropertyRemoteSource, PhotoRemoteSource> {
        return DataSource(propertySource = propertyRemoteSource, photoSource = photoRemoteSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideCacheDataSource(
        propertyCacheSource: PropertyCacheSource,
        photoCacheSource: PhotoCacheSource
    ): DataSource<PropertyCacheSource, PhotoCacheSource> {
        return DataSource(propertySource = propertyCacheSource, photoSource = photoCacheSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePropertyRepository(
        networkConnectionLiveData: NetworkConnectionLiveData,
        remoteDataSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>,
        cacheDataSource: DataSource<PropertyCacheSource, PhotoCacheSource>
    ): PropertyRepository = DefaultPropertyRepository(
        networkConnectionLiveData = networkConnectionLiveData,
        remoteDataSource = remoteDataSource,
        cacheDataSource = cacheDataSource
    )
}
