package com.openclassrooms.realestatemanager.ui.property.properties

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhaarman.mockito_kotlin.mock
import com.openclassrooms.realestatemanager.data.repository.PropertyRepository
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import com.openclassrooms.realestatemanager.util.schedulers.BaseSchedulerProvider
import com.openclassrooms.realestatemanager.util.schedulers.ImmediateSchedulerProvider
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.apache.commons.lang3.tuple.MutablePair
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`

@RunWith(JUnit4::class)
class PropertiesViewModelTest {

    private lateinit var propertiesViewModel: PropertiesViewModel
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertiesProcessor: PropertiesActionProcessor
    private lateinit var testObserver: TestObserver<PropertiesViewState>
    private lateinit var fakeProperties: List<Property>
    private lateinit var jsonUtil: JsonUtil
    private lateinit var schedulerProvider: BaseSchedulerProvider

    @Before
     fun setUp() {
        jsonUtil = JsonUtil()

        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)

        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.id }

        // Make the sure that all schedulers are immediate.
        schedulerProvider = ImmediateSchedulerProvider()

        propertyRepository = mock()

        propertiesProcessor = PropertiesActionProcessor(propertyRepository, schedulerProvider)
        propertiesViewModel = PropertiesViewModel(propertiesProcessor)

        testObserver = propertiesViewModel.states().test()
    }

    @Test
    fun given_properties_when_load_all_properties_then_intent_return_success() {
        // Given that properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.just(MutablePair(null as Boolean?, fakeProperties.toMutableList())))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state is in Success status
        testObserver.assertValueAt(2) { state -> state.properties == fakeProperties }
    }

    @Test
    fun given_properties_when_load_all_properties_then_returns_loading() {
        // Given that properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.just(MutablePair(null as Boolean?, fakeProperties.toMutableList())))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then progress indicator state is emitted
        testObserver.assertValueAt(1, PropertiesViewState::inProgress)
    }

    @Test
    fun given_properties_when_load_all_properties_then_intent_when_success_is_not_in_progress() {
        // Given that properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.just(MutablePair(null as Boolean?, fakeProperties.toMutableList())))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state is not in Progress status
        testObserver.assertValueAt(2) { state -> !state.inProgress }
    }

    @Test
    fun given_properties_when_load_all_properties_then_intent_return_data() {
        // Given that properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.just(MutablePair(null as Boolean?, fakeProperties.toMutableList())))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        //Then properties are equal to fake properties
        testObserver.assertValueAt(2) { state -> state.properties == fakeProperties  }
    }

    @Test
    fun given_properties_when_load_all_properties_then_returns_error() {
        // Given that no properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.error(Exception()))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state is in Failed status
        testObserver.assertValueAt(2)  { state -> state.error != null }
    }

    @Test
    fun given_properties_when_error_then_is_not_in_progress() {
        // Given that no properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.error(Exception()))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state is not in Progress status
        testObserver.assertValueAt(2) { state -> !state.inProgress }
    }

    @Test
    fun given_properties_when_error_then_is_not_contains_data() {
        // Given that no properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.error(Exception()))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state properties are null
        testObserver.assertValueAt(2) { state -> state.properties == null }
    }

    @Test
    fun given_properties_when_load_all_properties_then_intent_begin_as_idle() {
        // Given that no properties are available in the repository
        `when`(propertyRepository.updatePropertiesFromCache()).thenReturn(Observable.just(MutablePair(null as Boolean?, null)))
        `when`(propertyRepository.findAllProperties()).thenReturn(Observable.error(Exception()))

        // When properties are loaded
        propertiesViewModel.processIntents(Observable.just(PropertiesIntent.LoadPropertiesIntent))

        // Then state properties are null
        testObserver.assertValueAt(0) { state -> state == PropertiesViewState.idle() }
    }
}