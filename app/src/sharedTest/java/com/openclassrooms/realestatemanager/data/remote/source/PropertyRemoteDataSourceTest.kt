package com.openclassrooms.realestatemanager.data.remote.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyRemoteDataSourceTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>
    private lateinit var firestore : FirebaseFirestore

    private lateinit var remoteDataSource: PropertyRemoteDataSource

    @Before
    fun initApiService() {
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false)
            .build()

        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = settings

        remoteDataSource = PropertyRemoteDataSource(firestore = firestore)

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)
        fakeProperties = fakeProperties.sortedBy { it.id }
    }

    @After
    fun closeApiService() {
        remoteDataSource.deleteAllProperties().blockingAwait()
        firestore.terminate()
    }

    @Test
    fun given_remote_data_source_when_save_a_property_then_saved_successfully() {
        // Given properties list and When properties list saved
        remoteDataSource.saveProperty(fakeProperties[0]).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(remoteDataSource.count().blockingGet()).isEqualTo(1)
    }

    @Test
    fun given_remote_data_source_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(remoteDataSource.count().blockingGet()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_remote_data_source_when_find_all_properties_then_found_successfully() {
        // Given properties list
        fakeProperties = fakeProperties.sortedBy { it.id }

        // When properties list saved
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        var actualProperties = remoteDataSource.findAllProperties().blockingGet()

        // Then returned properties in database is equal to given properties list
        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    @Test
    fun given_remote_data_source_when_find_property_by_id_then_found_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val property = fakeProperties[fakeProperties.indices.random()]
        val expectedProperty: Property = remoteDataSource.findPropertyById(property.id).blockingGet()
        assertThat(expectedProperty).isEqualTo(property)
    }

    @Test
    fun given_remote_data_source_when_find_properties_by_ids_then_found_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        val expectedProperties: List<Property> = remoteDataSource.findPropertiesByIds(propertyIds).blockingGet()
        assertThat(expectedProperties).isEqualTo(fakeProperties.subList(0, 2))
    }

    @Test
    fun given_remote_data_source_when_update_property_then_updated_successfully() {
        val initialProperty = fakeProperties[fakeProperties.indices.random()]

        remoteDataSource.saveProperty(initialProperty).blockingAwait()

        val updatedProperty = initialProperty.copy()
        with(updatedProperty) {
            description = "new description"
            type = PropertyType.values().first { type -> type != initialProperty.type }
        }
        remoteDataSource.updateProperty(updatedProperty).blockingAwait()

        val finalProperty = remoteDataSource.findPropertyById(initialProperty.id).blockingGet()
        assertThat(finalProperty).isEqualTo(updatedProperty)
    }

    @Test
    fun given_remote_data_source_when_update_properties_then_updated_successfully() {
        val initialProperties = Array(2) { fakeProperties[fakeProperties.indices.random()] }

        remoteDataSource.saveProperties(initialProperties.asList()).blockingAwait()

        var updatedProperties = initialProperties.copyOf().toList()
        updatedProperties.forEachIndexed { index,  updatedProperty ->
            with(updatedProperty) {
                description = "new description"
                type = PropertyType.values().first { type -> type != initialProperties[index].type }
            }
        }
        updatedProperties = updatedProperties.sortedBy { it.id }

        remoteDataSource.updateProperties(updatedProperties).blockingAwait()

        var finalProperties = remoteDataSource.findAllProperties().blockingGet()
        finalProperties = finalProperties.sortedBy { it.id }

        assertThat(finalProperties).isEqualTo(updatedProperties.toList())
    }

    @Test
    fun given_remote_data_source_when_delete_property_by_id_then_deleted_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        assertThat(remoteDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)
        val property = fakeProperties[fakeProperties.indices.random()]
        remoteDataSource.deleteById(property.id).blockingAwait()
        assertThat(remoteDataSource.findAllProperties().blockingGet().contains(property))
            .isFalse()
    }

    @Test
    fun given_remote_data_source_when_delete_properties_by_ids_then_deleted_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        remoteDataSource.deletePropertiesByIds(propertyIds).blockingAwait()

        val findAllProperties = remoteDataSource.findAllProperties().blockingGet()
        assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
        assertThat(findAllProperties.containsAll(fakeProperties.subList(0, 2))).isFalse()
    }

    @Test
    fun given_remote_data_source_when_delete_properties_then_deleted_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()
        assertThat(remoteDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)

        remoteDataSource.deleteProperties(fakeProperties.subList(0, 2)).blockingAwait()

        val findAllProperties = remoteDataSource.findAllProperties().blockingGet()
        assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
    }

    @Test
    fun given_remote_data_source_when_delete_all_properties_then_deleted_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()
        assertThat(
            remoteDataSource.findAllProperties().blockingGet().size
        ).isEqualTo(fakeProperties.size)
        remoteDataSource.deleteAllProperties().blockingAwait()
        assertThat(remoteDataSource.findAllProperties().blockingGet()).isEmpty()
    }
}