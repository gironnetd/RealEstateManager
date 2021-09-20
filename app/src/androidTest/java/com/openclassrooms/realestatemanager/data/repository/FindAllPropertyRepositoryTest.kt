package com.openclassrooms.realestatemanager.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.cache.source.PhotoCacheSource
import com.openclassrooms.realestatemanager.data.cache.source.PropertyCacheSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoDataSource
import com.openclassrooms.realestatemanager.data.fake.photo.FakePhotoStorageSource
import com.openclassrooms.realestatemanager.data.fake.property.FakePropertyDataSource
import com.openclassrooms.realestatemanager.data.remote.source.PhotoRemoteSource
import com.openclassrooms.realestatemanager.data.remote.source.PropertyRemoteSource
import com.openclassrooms.realestatemanager.data.source.DataSource
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.*
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.switchAllNetworks
import com.openclassrooms.realestatemanager.util.ConnectivityUtil.waitInternetStateChange
import com.openclassrooms.realestatemanager.util.Constants.TIMEOUT_INTERNET_CONNECTION
import com.openclassrooms.realestatemanager.util.ConstantsTest.PHOTOS_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import io.reactivex.Completable
import io.reactivex.Completable.concatArray
import io.reactivex.observers.TestObserver
import junit.framework.TestCase
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.*
import timber.log.Timber.Forest.tag
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FindAllPropertyRepositoryTest : TestCase() {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    @Inject lateinit var jsonUtil: JsonUtil

    private lateinit var fakeProperties: List<Property>

    private lateinit var networkConnectionLiveData: NetworkConnectionLiveData
    private lateinit var remoteSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>
    private lateinit var cacheSource: DataSource<PropertyCacheSource, PhotoCacheSource>
    private lateinit var propertyRepository: DefaultPropertyRepository

    var testApplication : TestBaseApplication = InstrumentationRegistry
        .getInstrumentation()
        .targetContext
        .applicationContext as TestBaseApplication

    private lateinit var testObserver: TestObserver<List<Property>>

    @Before
    public override fun setUp() {
        super.setUp()

        injectTest(testApplication)

        var rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson,
            object : TypeToken<List<Property>>() {}.type)
        rawJson = jsonUtil.readJSONFromAsset(PHOTOS_DATA_FILENAME)

        fakeProperties.forEach { property ->
            val photos: List<Photo> = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
            val mainPhoto = photos.single { photo -> photo.mainPhoto }

            mainPhoto.bitmap = BitmapUtil.bitmapFromAsset(testApplication.applicationContext, mainPhoto.id)
            property.photos.add(mainPhoto)
        }
        fakeProperties = fakeProperties.sortedBy { it.id }
        ConnectivityUtil.context = testApplication.applicationContext
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
    fun given_property_repository_when_has_internet_and_local_storage_is_not_empty_then_inspect_behavior() {
        // Given PropertyRepository and When has internet and local storage is not empty
        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(
                cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))
        )

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    testObserver = propertyRepository.findAllProperties().test()

                    testObserver.assertValueAt(0) {
                        verify(cacheSource).findAll(Property::class)
                        verify(remoteSource).findAll(Property::class)
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_internet_and_local_storage_is_not_empty_then_inspect_data_result() {
        // Given PropertyRepository and When has internet and local storage is not empty

        remoteSource = DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))


        cacheSource = DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    // Then inspect repository data result with Truth
                    propertyRepository.findAllProperties().map { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_not_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        assertThat(fakeProperties).isEqualTo(returnedProperties)
                        tag(TAG).d("returned properties is equal to fakeProperties")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_local_storage_is_not_empty_then_inspect_behavior() {
        // Given PropertyRepository and When has no internet and local storage is not empty

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))
        )

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    // Then inspect repository behavior with Mockito
                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_not_empty **/")
                        verify(cacheSource).findAll(Property::class)
                        verify(cacheSource, never()).save(any(Property::class), anyList())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_local_storage_is_not_empty_then_inspect_data_result() {
        // Given PropertyRepository and When has no internet and local storage is not empty

        remoteSource = DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))


        cacheSource = DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    // Then inspect repository data result with Truth
                    propertyRepository.findAllProperties().map { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_no_internet_and_local_storage_is_not_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        assertThat(fakeProperties).isEqualTo(returnedProperties)
                        tag(TAG).i("returned properties is equal to fakeProperties")
                        returnedProperties
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_internet_and_local_storage_is_empty_then_inspect_behavior() {
        // Given PropertyRepository and When has internet and local storage is empty

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource.deleteAll(Property::class).blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )


                    testObserver = propertyRepository.findAllProperties().test()

                    // Then inspect repository behavior with Mockito
                    testObserver.assertValueAt(0) {
                        verify(cacheSource, atLeastOnce()).findAll(Property::class)
                        true
                    }

                    testObserver.assertValueAt(1) {
                        tag(TAG).i("/** behavior_when_has_internet_and_local_storage_is_empty **/")
                        verify(remoteSource).findAll(Property::class)
                        verify(cacheSource/*, times(2)*/).findAll(Property::class)
                        verify(cacheSource).save(any(Property::class), anyList())
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_internet_and_local_storage_is_empty_then_inspect_data_result() {
        // Given PropertyRepository and When has internet and local storage is empty

        remoteSource = DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))

        cacheSource = DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))

        cacheSource.deleteAll(Property::class).blockingAwait()

        concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    testObserver = propertyRepository.findAllProperties().test()

                    // Then inspect repository data result with Truth
                    testObserver.assertValueAt(1) { returnedProperties ->
                        tag(TAG).i("/** data_result_when_has_internet_and_local_storage_is_empty **/")
                        assertThat(returnedProperties).isNotNull()
                        tag(TAG).i("returned properties is not null")
                        assertThat(returnedProperties).isNotEmpty()
                        tag(TAG).i("returned properties is not empty")
                        assertThat(fakeProperties).isEqualTo(returnedProperties)
                        tag(TAG).i("returned properties is equal to fakeProperties")
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_local_storage_is_empty_then_inspect_behavior() {
        // Given PropertyRepository and When has no internet and local storage is empty

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))
        )

        cacheSource.deleteAll(Property::class).blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    // Then inspect repository behavior with Mockito
                    propertyRepository.findAllProperties().map {
                        tag(TAG).i("/** behavior_when_has_no_internet_and_local_storage_is_empty **/")
                        verify(cacheSource).findAll(Property::class)
                        verify(cacheSource, never()).save(any(Property::class), anyList())
                        it
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_local_storage_is_empty_then_inspect_data_result() {
        // Given PropertyRepository and When has no internet and local storage is empty

        remoteSource = DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil)))

        cacheSource = DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil)))

        cacheSource.deleteAll(Property::class).blockingAwait()

        concatArray(switchAllNetworks(false), waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    // Then inspect repository data result with Truth
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

    @Suppress("UNCHECKED_CAST")
    private fun <T> any(type: T): T {
        any<T>()
        return null as T
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    companion object {
        private val TAG = FindAllPropertyRepositoryTest::class.simpleName!!
    }
}