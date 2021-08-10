package com.openclassrooms.realestatemanager.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.PropertyFactory.Factory.createProperty
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
import io.reactivex.Completable
import junit.framework.TestCase
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreatePropertyRepositoryTest : TestCase() {

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

    @Before
    public override fun setUp() {
        super.setUp()

        injectTest(testApplication)

        var rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson,
            object : TypeToken<List<Property>>() {}.type)
        rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)

        fakeProperties.forEach { property ->
            val photos: List<Photo> = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

            photos.forEach { photo -> photo.bitmap = BitmapUtil.bitmapFromAsset(
                InstrumentationRegistry.getInstrumentation().targetContext,
                photo.id)
            }

            property.photos.addAll(photos)
        }
        fakeProperties = fakeProperties.sortedBy { it.id }
        ConnectivityUtil.context = testApplication.applicationContext
    }

    @After
    public override fun tearDown() {
        if(networkConnectionLiveData.value != true) {
            Completable.concatArray(switchAllNetworks(true),
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
    fun given_property_repository_when_has_internet_and_create_properties_then_inspect_behavior_and_data_result() {
        // Given PropertyRepository and When has internet

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(
                cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        Completable.concatArray(switchAllNetworks(true),
            waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    val firstProperty = createProperty(fakeProperties)
                    val firstUpdatedPhotos = firstProperty.photos.filter { photo -> photo.updated }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.createProperty(firstProperty).map { isTotallyCreated ->
                        firstProperty.updated = false
                        firstUpdatedPhotos.forEach { photo -> photo.updated = false }

                        verify(remoteSource).save(Property::class, firstProperty)
                        verify(remoteSource).save(Photo::class, firstUpdatedPhotos)
                        verify(cacheSource).save(Property::class, firstProperty)
                        verify(cacheSource).update(Photo::class, firstUpdatedPhotos)
                        assertThat(isTotallyCreated).isTrue()
                        true
                    }.blockingFirst()

                    val secondProperty = createProperty(fakeProperties)
                    val secondUpdatedPhotos = secondProperty.photos.filter { photo -> photo.updated }

                    propertyRepository.createProperty(secondProperty).map { isTotallyCreated ->
                        secondProperty.updated = false
                        secondUpdatedPhotos.forEach { photo -> photo.updated = false }

                        verify(remoteSource, atLeast(1)).save(Property::class, secondProperty)
                        verify(remoteSource, atLeast(1)).save(Photo::class, secondUpdatedPhotos)
                        verify(cacheSource, atLeast(1)).save(Property::class, secondProperty)
                        verify(cacheSource, atLeast(1)).save(Photo::class, secondUpdatedPhotos)
                        assertThat(isTotallyCreated).isTrue()
                        true
                    }.blockingFirst()

                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_internet_and_create_property_without_photos_then_inspect_behavior_and_data_result() {
        // Given PropertyRepository and When has internet

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(
                cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        Completable.concatArray(switchAllNetworks(true),
            waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    val property = createProperty(fakeProperties)
                    property.photos.forEach { photo -> photo.updated = false }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.createProperty(property).map { isTotallyCreated ->
                        property.updated = false

                        verify(remoteSource).save(Property::class, property)
                        verify(remoteSource, never()).save(any(Photo::class), anyList())
                        verify(cacheSource).save(Property::class, property)
                        verify(cacheSource, never()).save(any(Photo::class), anyList())
                        assertThat(isTotallyCreated).isTrue()
                        true
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_create_properties_then_inspect_behavior_and_data_result() {
        // Given PropertyRepository and When has no internet

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(
                cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        Completable.concatArray(switchAllNetworks(false),
            waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    val firstProperty = createProperty(fakeProperties)
                    val firstUpdatedPhotos = firstProperty.photos.filter { photo -> photo.updated }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.createProperty(firstProperty).map { isTotallyCreated ->
                        verify(cacheSource).save(Property::class, firstProperty)
                        verify(cacheSource).save(Photo::class, firstUpdatedPhotos)
                        assertThat(isTotallyCreated).isFalse()
                        true
                    }.blockingFirst()

                    val secondProperty = createProperty(fakeProperties)
                    val secondUpdatedPhotos = secondProperty.photos.filter { photo -> photo.updated }

                    propertyRepository.createProperty(secondProperty).map { isTotallyCreated ->
                        verify(cacheSource).save(Property::class, secondProperty)
                        verify(cacheSource, atLeast(1)).save(Photo::class, secondUpdatedPhotos)
                        assertThat(isTotallyCreated).isFalse()
                        true
                    }.blockingFirst()

                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_create_properties_without_photos_then_inspect_behavior_and_data_result() {
        // Given PropertyRepository and When has no internet

        remoteSource = spy(DataSource(
            propertySource = PropertyRemoteSource(remoteData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoRemoteSource(
                remoteData = FakePhotoDataSource(jsonUtil),
                remoteStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        cacheSource = spy(DataSource(
            propertySource = PropertyCacheSource(cacheData = FakePropertyDataSource(jsonUtil)),
            photoSource = PhotoCacheSource(
                cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        Completable.concatArray(switchAllNetworks(false),
            waitInternetStateChange(false))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait().let {
                Completable.fromAction {

                    networkConnectionLiveData = NetworkConnectionLiveData(testApplication.applicationContext)

                    propertyRepository = DefaultPropertyRepository(
                        networkConnectionLiveData = networkConnectionLiveData,
                        remoteDataSource = remoteSource,
                        cacheDataSource = cacheSource
                    )

                    val property = createProperty(fakeProperties)
                    property.photos.forEach { photo -> photo.updated = false }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.createProperty(property).map { isTotallyCreated ->
                        verify(cacheSource).save(Property::class, property)
                        verify(cacheSource, never()).save(any(Photo::class), anyList())
                        assertThat(isTotallyCreated).isFalse()
                        true
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    private fun <T> any(type: T): T {
        ArgumentMatchers.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }
}