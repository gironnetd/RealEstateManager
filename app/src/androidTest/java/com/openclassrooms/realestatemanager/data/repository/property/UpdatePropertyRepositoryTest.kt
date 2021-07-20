package com.openclassrooms.realestatemanager.data.repository.property

import android.graphics.BitmapFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
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
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import junit.framework.TestCase
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.StringType
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UpdatePropertyRepositoryTest : TestCase() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    @Inject
    lateinit var jsonUtil: JsonUtil

    private lateinit var fakeProperties: List<Property>

    private lateinit var networkConnectionLiveData: NetworkConnectionLiveData
    private lateinit var remoteSource: DataSource<PropertyRemoteSource, PhotoRemoteSource>
    private lateinit var cacheSource: DataSource<PropertyCacheSource, PhotoCacheSource>
    private lateinit var propertyRepository: DefaultPropertyRepository

    var testApplication : TestBaseApplication = InstrumentationRegistry
        .getInstrumentation()
        .targetContext
        .applicationContext as TestBaseApplication

    lateinit var testObserver: TestObserver<Boolean>

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
    fun given_property_repository_when_has_internet_and_update_properties_then_inspect_behavior_and_data_result() {
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

                    val firstProperty = createUpdatedProperty()
                    val firstUpdatedPhotos = firstProperty.photos.filter { photo -> photo.updated }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.updateProperty(firstProperty).map { isTotallyUpdated ->
                        firstProperty.updated = false
                        firstUpdatedPhotos.forEach { photo -> photo.updated = false }

                        verify(remoteSource).update(Property::class, firstProperty)
                        verify(remoteSource).update(Photo::class, firstUpdatedPhotos)
                        verify(cacheSource).update(Property::class, firstProperty)
                        verify(cacheSource).update(Photo::class, firstUpdatedPhotos)
                        assertThat(isTotallyUpdated).isTrue()
                        true
                    }.blockingFirst()

                    val secondProperty = createUpdatedProperty()
                    val secondUpdatedPhotos = secondProperty.photos.filter { photo -> photo.updated }

                    propertyRepository.updateProperty(secondProperty).map { isTotallyUpdated ->
                        secondProperty.updated = false
                        secondUpdatedPhotos.forEach { photo -> photo.updated = false }

                        verify(remoteSource).update(Property::class, secondProperty)
                        verify(remoteSource).update(Photo::class, secondUpdatedPhotos)
                        verify(cacheSource).update(Property::class, secondProperty)
                        verify(cacheSource).update(Photo::class, secondUpdatedPhotos)
                        assertThat(isTotallyUpdated).isTrue()
                        true
                    }.blockingFirst()

                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_internet_and_update_properties_without_photos_then_inspect_behavior_and_data_result() {
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

                    val property = createUpdatedProperty()
                    property.photos.forEach { photo -> photo.updated = false }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.updateProperty(property).map { isTotallyUpdated ->
                        property.updated = false

                        verify(remoteSource).update(Property::class, property)
                        verify(remoteSource, never()).update(any(Photo::class), anyList())
                        verify(cacheSource).update(Property::class, property)
                        verify(cacheSource, never()).update(any(Photo::class), anyList())
                        assertThat(isTotallyUpdated).isTrue()
                        true
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }


    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_update_properties_then_inspect_behavior_and_data_result() {
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

                    val firstProperty = createUpdatedProperty()
                    val firstUpdatedPhotos = firstProperty.photos.filter { photo -> photo.updated }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.updateProperty(firstProperty).map { isTotallyUpdated ->
                        verify(cacheSource).update(Property::class, firstProperty)
                        verify(cacheSource).update(Photo::class, firstUpdatedPhotos)
                        assertThat(isTotallyUpdated).isFalse()
                        true
                    }.blockingFirst()

                    val secondProperty = createUpdatedProperty()
                    val secondUpdatedPhotos = secondProperty.photos.filter { photo -> photo.updated }

                    propertyRepository.updateProperty(secondProperty).map { isTotallyUpdated ->
                        verify(cacheSource).update(Property::class, secondProperty)
                        verify(cacheSource, atLeast(1)).update(Photo::class, secondUpdatedPhotos)
                        assertThat(isTotallyUpdated).isFalse()
                        true
                    }.blockingFirst()

                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_update_properties_without_photos_then_inspect_behavior_and_data_result() {
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

                    val property = createUpdatedProperty()
                    property.photos.forEach { photo -> photo.updated = false }

                    // Then inspect repository behavior with Mockito
                    propertyRepository.updateProperty(property).map { isTotallyUpdated ->
                        verify(cacheSource).update(Property::class, property)
                        verify(cacheSource, never()).update(any(Photo::class), anyList())
                        assertThat(isTotallyUpdated).isFalse()
                        true
                    }.blockingFirst()
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_switching_network_then_inspect_behavior_and_data_result() {
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
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        val property = createUpdatedProperty()
        val updatedPhotos = property.photos.filter { photo -> photo.updated }

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

                    // Then inspect repository behavior with Mockito
                    testObserver = propertyRepository.updateProperty(property).test()

                    testObserver.assertValueAt(0) { isTotallyUpdated ->
                        verify(cacheSource).update(Property::class, property)
                        verify(cacheSource).update(Photo::class, updatedPhotos)
                        assertThat(isTotallyUpdated).isFalse()
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }

        Completable.concatArray(switchAllNetworks(true), waitInternetStateChange(true))
            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
            .blockingAwait()
            .let {
                Completable.fromAction {

                    testObserver.assertValueAt(1) { isTotallyUpdated ->
                        verify(remoteSource).update(Property::class, property)
                        verify(remoteSource).update(Photo::class, updatedPhotos)
                        verify(cacheSource, times(2)).update(Property::class, property)
                        verify(cacheSource, times(2)).update(Photo::class, updatedPhotos)
                        assertThat(isTotallyUpdated).isTrue()
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    @Test
    @Suppress("UnstableApiUsage")
    fun given_property_repository_when_has_no_internet_and_update_properties_without_photos_and_switching_network_then_inspect_behavior_and_data_result() {
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
            photoSource = PhotoCacheSource(cacheData = FakePhotoDataSource(jsonUtil),
                cacheStorage = FakePhotoStorageSource(jsonUtil,
                    cacheDir = testApplication.cacheDir)))
        )

        val property = createUpdatedProperty()
        property.photos.forEach { photo -> photo.updated = false }

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

                    // Then inspect repository behavior with Mockito
                    testObserver = propertyRepository.updateProperty(property).test()

                    testObserver.assertValueAt(0) { isTotallyUpdated ->
                        verify(cacheSource).update(Property::class, property)
                        verify(cacheSource, never()).update(any(Photo::class), anyList())
                        assertThat(isTotallyUpdated).isFalse()

                        Completable.concatArray(switchAllNetworks(true), waitInternetStateChange(true))
                            .delay(TIMEOUT_INTERNET_CONNECTION.toLong(), TimeUnit.MILLISECONDS)
                            .blockingAwait()
                            .let {
                                Completable.fromAction {

                                    testObserver.assertValueAt(1) { isTotallyUpdated ->
                                        verify(remoteSource).update(Property::class, property)
                                        verify(remoteSource, never()).update(any(Photo::class), anyList())
                                        verify(cacheSource, times(2)).update(Property::class, property)
                                        verify(cacheSource, never()).update(any(Photo::class), anyList())
                                        assertThat(isTotallyUpdated).isTrue()
                                        true
                                    }
                                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 2), TimeUnit.MILLISECONDS)
                                    .blockingAwait()
                            }
                        true
                    }
                }.delaySubscription((TIMEOUT_INTERNET_CONNECTION.toLong() * 3), TimeUnit.MILLISECONDS)
                    .blockingAwait()
            }
    }

    private fun createUpdatedProperty(): Property {
        val property = fakeProperties.random()
        property.updated = true
        val mockNeat = MockNeat.threadLocal()

        property.description = mockNeat.strings().size(40).type(StringType.LETTERS).get()
        property.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

        property.address!!.street = mockNeat.strings().size(12).type(StringType.LETTERS).get()
        property.address!!.city = mockNeat.cities().capitalsEurope().get()
        property.address!!.postalCode = mockNeat.strings().size(5).type(StringType.NUMBERS).get()
        property.address!!.country = mockNeat.countries().names().get()
        property.address!!.state = mockNeat.usStates().get()

        property.bathRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
        property.bedRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
        property.rooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()

        property.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

        val updatedPhotoId = property.photos.random().id

        with(property.photos.single { photo -> photo.id == updatedPhotoId }) {
            bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getInstrumentation().targetContext.resources, R.drawable.default_image)
            updated = true
        }

        return property
    }

    private fun <T> any(type: T): T {
        any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    companion object {
        private val TAG = UpdatePropertyRepositoryTest::class.simpleName
    }

}