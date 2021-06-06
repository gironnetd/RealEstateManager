package com.openclassrooms.realestatemanager.di.property.browse

import android.content.Context
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.PropertyLocalDataSource
import com.openclassrooms.realestatemanager.data.remote.DefaultPropertyApiService
import com.openclassrooms.realestatemanager.data.remote.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.data.repository.property.DefaultPropertyRepository
import com.openclassrooms.realestatemanager.data.repository.property.PropertyRepository
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
    fun providePropertyRemoteDataSource(apiService: DefaultPropertyApiService): PropertyRemoteDataSource
            = PropertyRemoteDataSource(apiService = apiService)

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyLocalDataSource(db: AppDatabase, context: Context): PropertyLocalDataSource
            = PropertyLocalDataSource(database = db, context = context)

    @JvmStatic
    @BrowseScope
    @Provides
    fun providePropertyRepository(networkConnectionLiveData: NetworkConnectionLiveData,
                                  propertyRemoteDataSource: PropertyRemoteDataSource,
                                  propertyLocalDataSource: PropertyLocalDataSource
    ): PropertyRepository
        = DefaultPropertyRepository(networkConnectionLiveData = networkConnectionLiveData,
            propertyRemoteDataSource = propertyRemoteDataSource,
            propertyLocalDataSource = propertyLocalDataSource)
}