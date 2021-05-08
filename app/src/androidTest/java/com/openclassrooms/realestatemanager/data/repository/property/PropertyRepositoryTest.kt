package com.openclassrooms.realestatemanager.data.repository.property

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.api.property.FakePropertyApiService
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.PropertyLocalDataSource
import com.openclassrooms.realestatemanager.data.remote.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConnectivityUtil
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.Companion.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.Companion.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.Constants.TIMEOUT_INTERNET_CONNECTION
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import com.openclassrooms.realestatemanager.util.ConstantsTest.PICTURES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import junit.framework.TestCase
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import timber.log.Timber.tag
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PropertyRepositoryTest : TestCase() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var apiService: FakePropertyApiService
    private lateinit var database: AppDatabase
    @Inject lateinit var jsonUtil: JsonUtil

    private lateinit var fakeProperties: List<Property>

    private lateinit var networkConnectionLiveData: NetworkConnectionLiveData
    private lateinit var remoteDataSource: PropertyRemoteDataSource
    private lateinit var localDataSource: PropertyLocalDataSource
    private lateinit var propertyRepository: DefaultPropertyRepository

    var app : TestBaseApplication = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext as TestBaseApplication

    @Before
    public override fun setUp() {
        super.setUp()

        injectTest(app)

        apiService = FakePropertyApiService(jsonUtil = jsonUtil)
        apiService.propertiesJsonFileName = PROPERTIES_DATA_FILENAME

        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build()

        var rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )

        rawJson = jsonUtil.readJSONFromAsset(PICTURES_DATA_FILENAME)

        fakeProperties.forEachIndexed { index, property ->

            val pictures: List<Picture> = Gson().fromJson(rawJson, object : TypeToken<List<Picture>>() {}.type)
            pictures.forEach { picture ->
                picture.propertyId = property.id
            }

            fakeProperties[index].pictures.addAll(pictures)
        }

        fakeProperties = fakeProperties.sortedBy { it.id }

        ConnectivityUtil.context = app.applicationContext
    }

    @After
    public override fun tearDown() {
        if(networkConnectionLiveData.value != true) {
            concatArray(switchAllNetworks(true),
                    waitInternetStateChange(true))
                    .blockingAwait().let {
                        super.tearDown()
                    }
        } else {
            super.tearDown()
        }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun behavior_when_has_internet_and_local_storage_is_not_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(database = database))

        Completable.fromAction {
            localDataSource.database.propertyDao().saveProperties(properties = fakeProperties)
            fakeProperties.forEach { property ->
                localDataSource.database.pictureDao().savePictures(property.pictures)
            }
        }.blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_internet_and_local_storage_is_not_empty **/")
                        verify(remoteDataSource).findAllProperties()
                        verify(localDataSource).findAllProperties()
                        verify(localDataSource, never()).saveProperties(any())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun data_result_when_has_internet_and_local_storage_is_not_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(database = database)

        Completable.fromAction {
            localDataSource.database.propertyDao().saveProperties(properties = fakeProperties)
            fakeProperties.forEach { property ->
                localDataSource.database.pictureDao().savePictures(property.pictures)
            }
        }.blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_not_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        fakeProperties.forEachIndexed { index, property ->
                            assertThat(property).isEqualTo(returnedProperties[index])
                        }
                        tag(TAG).d("returned properties is equal to fakeProperties")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun behavior_when_has_no_internet_and_local_storage_is_not_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(database = database))

        Completable.fromAction {
            localDataSource.database.propertyDao().saveProperties(properties = fakeProperties)
            fakeProperties.forEach { property ->
                localDataSource.database.pictureDao().savePictures(property.pictures)
            }
        }.blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_not_empty **/")
                        verify(localDataSource).findAllProperties()
                        verify(localDataSource, never()).saveProperties(any())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun data_result_when_has_no_internet_and_local_storage_is_not_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(database = database)

        localDataSource.saveProperties(properties = fakeProperties).blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_no_internet_and_local_storage_is_not_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        fakeProperties.forEachIndexed { index, property ->
                            assertThat(property).isEqualTo(returnedProperties[index])
                        }
                        tag(TAG).i("returned properties is equal to fakeProperties")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun behavior_when_has_internet_and_local_storage_is_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(database = database))

        localDataSource.deleteAllProperties().blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_internet_and_local_storage_is_empty **/")
                        verify(remoteDataSource).findAllProperties()
                        verify(localDataSource).findAllProperties()
                        verify(localDataSource).saveProperties(any())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun data_result_when_has_internet_and_local_storage_is_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(database = database)

        localDataSource.deleteAllProperties().blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map {returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        fakeProperties.forEachIndexed { index, property ->
                            assertThat(property).isEqualTo(returnedProperties[index])
                        }
                        tag(TAG).i("returned properties is equal to fakeProperties")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun behavior_when_has_no_internet_and_local_storage_is_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(database = database))

        localDataSource.deleteAllProperties().blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_empty **/")
                        verify(localDataSource).findAllProperties()
                        verify(localDataSource, never()).saveProperties(any())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun data_result_when_has_no_internet_and_local_storage_is_empty() {

        apiService.propertiesJsonFileName = EMPTY_LIST

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(database = database)

        localDataSource.deleteAllProperties().blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        propertyRemoteDataSource = remoteDataSource,
                        propertyLocalDataSource = localDataSource
                    )

                    propertyRepository.findAllProperties().doOnSubscribe {
                        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                            .blockingAwait()
                    }.map { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_no_internet_and_local_storage_is_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isEmpty()
                        tag(TAG).i("returned properties is empty")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }

    companion object {
        private val TAG = PropertyRepositoryTest::class.simpleName
    }
}