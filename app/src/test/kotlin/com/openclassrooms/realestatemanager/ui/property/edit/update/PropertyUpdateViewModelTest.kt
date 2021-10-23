package com.openclassrooms.realestatemanager.ui.property.edit.update

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.mock
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.models.property.Property
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditIntent
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTIES_FULLY_UPDATED
import com.openclassrooms.realestatemanager.ui.property.edit.PropertyEditViewState.UiNotification.PROPERTY_LOCALLY_UPDATED
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
class PropertyUpdateViewModelTest {

    private lateinit var propertyUpdateViewModel: PropertyUpdateViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertyUpdateActionProcessor: PropertyUpdateActionProcessor
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

        propertyUpdateActionProcessor = PropertyUpdateActionProcessor(propertyRepository, schedulerProvider)
        propertyUpdateViewModel = PropertyUpdateViewModel(propertyUpdateActionProcessor)

        testObserver = propertyUpdateViewModel.states().test()
    }

    @Test
    fun given_properties_when_update_a_property_and_has_no_internet_then_return_updated_partially() {
        val property = fakeProperties.random()
        `when`(propertyRepository.updateProperty(property)).thenReturn(Observable.just(false))

        propertyUpdateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent(property = property))
        )

        testObserver.assertValueAt(1) { state -> state.uiNotification == PROPERTY_LOCALLY_UPDATED }
    }

    @Test
    fun given_properties_when_update_a_property_and_has_internet_then_return_updated_totally() {
        val property = fakeProperties.random()
        `when`(propertyRepository.updateProperty(property)).thenReturn(Observable.just(true))

        propertyUpdateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent(property = property))
        )

        testObserver.assertValueAt(1) { state -> state.uiNotification == PROPERTIES_FULLY_UPDATED }
    }

    @Test
    fun given_properties_when_update_a_property_and_error_occurs_then_returns_error() {
        // Given error on update a property
        val property = fakeProperties.random()
        `when`(propertyRepository.updateProperty(property)).thenReturn(Observable.error(Exception()))

        // When properties are updated
        propertyUpdateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent(property = property))
        )

        // Then state is in Failed status
        testObserver.assertValueAt(1)  { state -> state.error != null }
    }

    @Test
    fun given_properties_when_update_a_property_and_error_occurs_then_is_not_in_progress() {
        // Given error on update a property
        val property = fakeProperties.random()
        `when`(propertyRepository.updateProperty(property)).thenReturn(Observable.error(Exception()))

        // When properties are updated
        propertyUpdateViewModel.processIntents(Observable.just(
            PropertyEditIntent.PropertyUpdateIntent.UpdatePropertyIntent(property = property))
        )

        // Then state is not in Progress status
        testObserver.assertValueAt(1) { state -> !state.inProgress }
    }
}