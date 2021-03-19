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
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.data.remote.PropertyRemoteDataSource
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConnectivityUtil
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.Companion.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.Companion.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.ConstantsTest.EMPTY_LIST
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import com.openclassrooms.realestatemanager.util.NetworkConnectionLiveData
import com.openclassrooms.realestatemanager.util.Utils.isInternetAvailable
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import junit.framework.TestCase
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import timber.log.Timber
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PropertyRepositoryTest : TestCase() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var apiService: FakePropertyApiService
    lateinit var propertyDao: PropertyDao
    @Inject lateinit var jsonUtil: JsonUtil

    private lateinit var fakeProperties: List<Property>

    lateinit var networkConnectionLiveData: NetworkConnectionLiveData
    lateinit var remoteDataSource: PropertyRemoteDataSource
    lateinit var localDataSource: PropertyLocalDataSource
    lateinit var propertyRepository: DefaultPropertyRepository

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

        propertyDao = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build().propertyDao()

        networkConnectionLiveData = NetworkConnectionLiveData(app.applicationContext)

        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.id }

        ConnectivityUtil.context = app.applicationContext

        Completable.fromCallable {
            if(!isInternetAvailable()) {
                concatArray(switchAllNetworks(true),
                        waitInternetStateChange(true))
                        .blockingAwait()
            }
        }.blockingAwait()
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
    fun behavior_when_has_internet_and_local_storage_is_not_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(propertyDao = propertyDao))

        Completable.fromAction {
            localDataSource.propertyDao.saveProperties(fakeProperties)
        }.blockingAwait()

         propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
         )

        propertyRepository.findAllProperties().map {
            Timber.tag(TAG).i("/** behavior_when_has_internet_and_local_storage_is_not_empty **/")
            verify(remoteDataSource, {
                Timber.tag(TAG).i("remoteDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource, {
                Timber.tag(TAG).i("localDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource,  {
                never()
                Timber.tag(TAG).i("localDataSource saveProperties() is never called")
            }).saveProperties(any())
            it
        }.blockingFirst()
    }

    @Test
    fun data_result_when_has_internet_and_local_storage_is_not_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(propertyDao = propertyDao)

        Completable.fromAction {
            localDataSource.propertyDao.saveProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().map { returnedProperties ->
            Timber.tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_not_empty **/")
            assertThat(returnedProperties).isNotNull()
            Timber.tag(TAG).i("returned properties is not null")
            assertThat(returnedProperties).isNotEmpty()
            Timber.tag(TAG).i("returned properties is not empty")
            fakeProperties.forEachIndexed { index, property ->
                assertThat(property).isEqualTo(returnedProperties[index])
            }
            Timber.tag(TAG).d("returned properties is equal to fakeProperties")
            returnedProperties
        }.blockingFirst()
    }

    @Test
    fun behavior_when_has_no_internet_and_local_storage_is_not_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(propertyDao = propertyDao))

        Completable.fromAction {
            localDataSource.propertyDao.saveProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().doOnSubscribe {
            concatArray(switchAllNetworks(false),
                    waitInternetStateChange(false))
                    .blockingAwait()
        }.map {
            Timber.tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_not_empty **/")
            verify(localDataSource, {
                Timber.tag(TAG).i("localDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource,  {
                never()
                Timber.tag(TAG).i("localDataSource saveProperties() is never called")
            }).saveProperties(any())
            it
        }.blockingFirst()
    }

    @Test
    fun data_result_when_has_no_internet_and_local_storage_is_not_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(propertyDao = propertyDao)

        Completable.fromAction {
            localDataSource.propertyDao.saveProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().doOnSubscribe {
            concatArray(switchAllNetworks(false),
                    waitInternetStateChange(false))
                    .blockingAwait()
        }.map { returnedProperties ->
            Timber.tag(TAG).i("/** data_result_when_has_no_internet_and_local_storage_is_not_empty **/")
            assertThat(returnedProperties).isNotNull()
            Timber.tag(TAG).i("returned properties is not null")
            assertThat(returnedProperties).isNotEmpty()
            Timber.tag(TAG).i("returned properties is not empty")
            fakeProperties.forEachIndexed { index, property ->
                assertThat(property).isEqualTo(returnedProperties[index])
            }
            Timber.tag(TAG).i("returned properties is equal to fakeProperties")
            returnedProperties
        }.blockingFirst()
    }

    @Test
    fun behavior_when_has_internet_and_local_storage_is_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(propertyDao = propertyDao))

        Completable.fromCallable {
            localDataSource.deleteAllProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().map {
            Timber.tag(TAG).i("/** behavior_when_has_internet_and_local_storage_is_empty **/")
            verify(remoteDataSource, {
                Timber.tag(TAG).i("remoteDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource, {
                Timber.tag(TAG).i("localDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource, {
                Timber.tag(TAG).i("localDataSource saveProperties() is called")
            }).saveProperties(any())
            it
        }.blockingFirst()
    }

    @Test
    fun data_result_when_has_internet_and_local_storage_is_empty() {

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(propertyDao = propertyDao)

        Completable.fromCallable {
            localDataSource.deleteAllProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().map {returnedProperties ->
            Timber.tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_empty **/")
            assertThat(returnedProperties).isNotNull()
            Timber.tag(TAG).i("returned properties is not null")
            assertThat(returnedProperties).isNotEmpty()
            Timber.tag(TAG).i("returned properties is not empty")
            fakeProperties.forEachIndexed { index, property ->
                assertThat(property).isEqualTo(returnedProperties[index])
            }
            Timber.tag(TAG).i("returned properties is equal to fakeProperties")
            returnedProperties
        }.blockingFirst()
    }

    @Test
    fun behavior_when_has_no_internet_and_local_storage_is_empty() {

        remoteDataSource = spy(PropertyRemoteDataSource(apiService = apiService))
        localDataSource = spy(PropertyLocalDataSource(propertyDao = propertyDao))

        Completable.fromCallable {
            localDataSource.deleteAllProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().doOnSubscribe {
            concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                    .blockingAwait()
        }.map {
            Timber.tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_empty **/")
            verify(localDataSource, {
                Timber.tag(TAG).i("localDataSource findAllProperties() is called")
            }).findAllProperties()
            verify(localDataSource,  {
                never()
                Timber.tag(TAG).i("localDataSource saveProperties() is never called")
            }).saveProperties(any())
            it
        }.blockingFirst()
    }

    @Test
    fun data_result_when_has_no_internet_and_local_storage_is_empty() {

        apiService.propertiesJsonFileName = EMPTY_LIST

        remoteDataSource = PropertyRemoteDataSource(apiService = apiService)
        localDataSource = PropertyLocalDataSource(propertyDao = propertyDao)

        Completable.fromAction {
            localDataSource.deleteAllProperties(fakeProperties)
        }.blockingAwait()

        propertyRepository = DefaultPropertyRepository(
                networkConnectionLiveData = networkConnectionLiveData,
                propertyRemoteDataSource = remoteDataSource,
                propertyLocalDataSource = localDataSource
        )

        propertyRepository.findAllProperties().doOnSubscribe {
            concatArray(switchAllNetworks(false), waitInternetStateChange(false))
                    .blockingAwait()
        }.map { returnedProperties ->
            Timber.tag(TAG).i("/** data_result_when_has_no_internet_and_local_storage_is_empty **/")
            assertThat(returnedProperties).isNotNull()
            Timber.tag(TAG).i("returned properties is not null")
            assertThat(returnedProperties).isEmpty()
            Timber.tag(TAG).i("returned properties is empty")
            returnedProperties
        }.blockingFirst()
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }

    companion object {
        private val TAG = PropertyRepositoryTest::class.simpleName
    }
}