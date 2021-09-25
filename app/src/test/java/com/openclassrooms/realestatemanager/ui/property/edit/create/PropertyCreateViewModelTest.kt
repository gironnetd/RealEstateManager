package com.openclassrooms.realestatemanager.ui.property.edit.create

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.mock
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
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

@RunWith(RobolectricTestRunner::class)
class PropertyCreateViewModelTest {

    private lateinit var propertyCreateViewModel: PropertyCreateViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertyCreateActionProcessor: PropertyCreateActionProcessor
    private lateinit var testObserver: TestObserver<PropertyEditViewState>
    private lateinit var fakeProperties: List<Property>
    private lateinit var jsonUtil: JsonUtil
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @Before
    fun setUp() {
        jsonUtil = JsonUtil()

        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PROPERTIES_DATA_FILENAME)

        fakeProperties = Gson().fromJson(
            rawJson,
            object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.id }

        // Make the sure that all schedulers are immediate.
        schedulerProvider = ImmediateSchedulerProvider()

        propertyRepository = mock()

        propertyCreateActionProcessor = PropertyCreateActionProcessor(propertyRepository, schedulerProvider)
        propertyCreateViewModel = PropertyCreateViewModel(propertyCreateActionProcessor)

        testObserver = propertyCreateViewModel.states().test()
    }

    @Test
    fun given_properties_when_create_a_property_and_has_no_internet_then_return_created_partially() {
        val property = fakeProperties.random()
        `when`(propertyRepository.createProperty(property)).thenReturn(Observable.just(false))

        propertyCreateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent(property = property))
        )

        testObserver.assertValueAt(2) { state -> state.uiNotification == PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_CREATED }
    }

    @Test
    fun given_properties_when_create_a_property_and_has_internet_then_return_created_totally() {
        val property = fakeProperties.random()
        `when`(propertyRepository.createProperty(property)).thenReturn(Observable.just(true))

        propertyCreateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent(property = property))
        )

        testObserver.assertValueAt(2) { state -> state.uiNotification == PropertyEditViewState.UiNotification.PROPERTIES_FULLY_CREATED }
    }

    @Test
    fun given_properties_when_create_a_property_and_error_occurs_then_returns_error() {
        // Given error on update a property
        val property = fakeProperties.random()
        `when`(propertyRepository.createProperty(property)).thenReturn(Observable.error(Exception()))

        // When properties are updated
        propertyCreateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent(property = property))
        )

        // Then state is in Failed status
        testObserver.assertValueAt(2)  { state -> state.error != null }
    }

    @Test
    fun given_properties_when_create_a_property_and_error_occurs_then_is_not_in_progress() {
        // Given error on update a property
        val property = fakeProperties.random()
        `when`(propertyRepository.createProperty(property)).thenReturn(Observable.error(Exception()))

        // When properties are updated
        propertyCreateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyCreateIntent.CreatePropertyIntent(property = property))
        )

        // Then state is not in Progress status
        testObserver.assertValueAt(2) { state -> !state.inProgress }
    }
}