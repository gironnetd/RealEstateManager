package com.openclassrooms.realestatemanager.ui.property.propertydetail

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.mock
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.ImmediateSchedulerProvider
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import kotlin.properties.Delegates

@RunWith(RobolectricTestRunner::class)
class PropertyDetailViewModelTest {

    private lateinit var propertyDetailViewModel: PropertyDetailViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertyDetailActionProcessor: PropertyDetailActionProcessor
    private lateinit var testObserver: TestObserver<PropertyDetailViewState>
    private lateinit var fakeProperties: List<Property>
    private var itemPosition by Delegates.notNull<Int>()
    private lateinit var jsonUtil: JsonUtil
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @Before
    fun setUp() {
        jsonUtil = JsonUtil()

        var rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PROPERTIES_DATA_FILENAME)

        fakeProperties = Gson().fromJson(
            rawJson,
            object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.id }

        rawJson =  jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)

        fakeProperties.forEachIndexed { index, property ->

            val photos: List<Photo> =
                Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

            photos.forEach { photo -> photo.propertyId = property.id }
            property.photos.addAll(photos)
        }

        itemPosition = (fakeProperties.indices).random()

            // Make the sure that all schedulers are immediate.
        schedulerProvider = ImmediateSchedulerProvider()

        propertyRepository = mock()

        propertyDetailActionProcessor = PropertyDetailActionProcessor(propertyRepository, schedulerProvider)
        propertyDetailViewModel = PropertyDetailViewModel(propertyDetailActionProcessor)

        testObserver = propertyDetailViewModel.states().test()
    }

    @Test
    fun given_properties_when_load_a_full_property_then_intent_return_success() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.just(fakeProperties[itemPosition]))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state property is equal to selected property
        testObserver.assertValueAt(1) { state -> state.property == fakeProperties[itemPosition] }
    }

    @Test
    fun given_properties_when_load__a_full_property_then_intent_when_success_is_not_in_progress() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.just(fakeProperties[itemPosition]))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state is not in Progress status
        testObserver.assertValueAt(1) { state -> !state.inProgress }
    }

    @Test
    fun given_properties_when_load__a_full_property_then_intent_return_data() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.just(fakeProperties[itemPosition]))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        //Then property are equal to selected fake property
        testObserver.assertValueAt(1) { state -> state.property == fakeProperties[itemPosition]  }
    }

    @Test
    fun given_properties_when_load__a_full_property_then_returns_error() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.error(Exception()))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state is in Failed status
        testObserver.assertValueAt(1)  { state -> state.error != null }
    }

    @Test
    fun given_properties_when_error_then_is_not_in_progress() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.error(Exception()))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state is not in Progress status
        testObserver.assertValueAt(1) { state -> !state.inProgress }
    }

    @Test
    fun given_properties_when_error_then_is_not_contains_data() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.error(Exception()))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state property are null
        testObserver.assertValueAt(1) { state -> state.property == null }
    }

    @Test
    fun given_properties_when_load_a_full_property_then_intent_begin_as_idle() {
        // Given that property to found is available in the repository
        `when`(propertyRepository.findProperty(fakeProperties[itemPosition].id)).thenReturn(Observable.just(fakeProperties[itemPosition]))

        // When property is loaded
        propertyDetailViewModel.processIntents(Observable.just(PropertyDetailIntent.PopulatePropertyIntent(fakeProperties[itemPosition].id)))

        // Then state is Idle
        testObserver.assertValueAt(0) { state -> state == PropertyDetailViewState.idle() }
    }
}