package com.openclassrooms.realestatemanager.data.remote.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_EMULATOR_HOST
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_FIRESTORE_PORT
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

    private lateinit var propertyRemoteDataSource: PropertyRemoteDataSource

    @Before
    fun initApiService() {
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_FIRESTORE_PORT)
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        firestore.firestoreSettings = settings

        propertyRemoteDataSource = PropertyRemoteDataSource(firestore = firestore)

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)
        fakeProperties = fakeProperties.sortedBy { it.id }
    }

    @After
    fun closeApiService() {
        propertyRemoteDataSource.deleteAllProperties().blockingAwait()
        firestore.terminate()
    }

    @Test
    fun given_property_remote_data_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        Truth.assertThat(propertyRemoteDataSource.count().blockingGet()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_property_remote_data_source_when_save_a_property_then_saved_successfully() {
        // Given properties list and When properties list saved
        propertyRemoteDataSource.saveProperty(fakeProperties[0]).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        Truth.assertThat(propertyRemoteDataSource.findPropertyById(fakeProperties[0].id).blockingGet()).isEqualTo(fakeProperties[0])
    }

    @Test
    fun given_property_remote_data_source_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        Truth.assertThat(propertyRemoteDataSource.findAllProperties().blockingGet()).isEqualTo(fakeProperties)
    }

    @Test
    fun given_property_remote_data_source_when_find_all_properties_then_found_successfully() {
        // Given properties list
        fakeProperties = fakeProperties.sortedBy { it.id }

        // When properties list saved
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        var actualProperties = propertyRemoteDataSource.findAllProperties().blockingGet()

        // Then returned properties in database is equal to given properties list
        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            Truth.assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    @Test
    fun given_property_remote_data_source_when_find_property_by_id_then_found_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val property = fakeProperties[fakeProperties.indices.random()]
        val expectedProperty: Property = propertyRemoteDataSource.findPropertyById(property.id).blockingGet()
        Truth.assertThat(expectedProperty).isEqualTo(property)
    }

    @Test
    fun given_property_remote_data_source_when_find_properties_by_ids_then_found_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        val expectedProperties: List<Property> = propertyRemoteDataSource.findPropertiesByIds(propertyIds).blockingGet()
        Truth.assertThat(expectedProperties).isEqualTo(fakeProperties.subList(0, 2))
    }

    @Test
    fun given_property_remote_data_source_when_update_property_then_updated_successfully() {
        val initialProperty = fakeProperties[fakeProperties.indices.random()]

        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        val updatedProperty = initialProperty.copy()
        with(updatedProperty) {
            description = "new description"
            type = PropertyType.values().first { type -> type != initialProperty.type }
        }
        propertyRemoteDataSource.updateProperty(updatedProperty).blockingAwait()

        val finalProperty = propertyRemoteDataSource.findPropertyById(initialProperty.id).blockingGet()
        Truth.assertThat(finalProperty).isEqualTo(updatedProperty)
    }

    @Test
    fun given_property_remote_data_source_when_update_properties_then_updated_successfully() {
        var initialProperties = arrayOf(fakeProperties[0], fakeProperties[1])

        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        var updatedProperties = initialProperties.copyOf().toList()
        updatedProperties.forEachIndexed { index,  updatedProperty ->
            with(updatedProperty) {
                description = "new description"
                type = com.openclassrooms.realestatemanager.models.PropertyType.values().first { type -> type != initialProperties[index].type }
            }
        }
        updatedProperties = updatedProperties.sortedBy { it.id }
        propertyRemoteDataSource.updateProperties(updatedProperties).blockingAwait()

        val ids = initialProperties.map { photo -> photo.id }
        var finalProperties = propertyRemoteDataSource.findAllProperties().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }
        finalProperties = finalProperties.sortedBy { it.id }

        Truth.assertThat(finalProperties).isEqualTo(updatedProperties.toList())
    }

    @Test
    fun given_property_remote_data_source_when_delete_property_by_id_then_deleted_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()

        Truth.assertThat(propertyRemoteDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)
        val property = fakeProperties[fakeProperties.indices.random()]
        propertyRemoteDataSource.deletePropertyById(property.id).blockingAwait()
        Truth.assertThat(propertyRemoteDataSource.findAllProperties().blockingGet().contains(property))
            .isFalse()
    }

    @Test
    fun given_property_remote_data_source_when_delete_properties_by_ids_then_deleted_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        propertyRemoteDataSource.deletePropertiesByIds(propertyIds).blockingAwait()

        val findAllProperties = propertyRemoteDataSource.findAllProperties().blockingGet()
        Truth.assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
        Truth.assertThat(findAllProperties.containsAll(fakeProperties.subList(0, 2))).isFalse()
    }

    @Test
    fun given_property_remote_data_source_when_delete_properties_then_deleted_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()
        Truth.assertThat(propertyRemoteDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)

        propertyRemoteDataSource.deleteProperties(fakeProperties.subList(0, 2)).blockingAwait()

        val findAllProperties = propertyRemoteDataSource.findAllProperties().blockingGet()
        Truth.assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
    }

    @Test
    fun given_property_remote_data_source_when_delete_all_properties_then_deleted_successfully() {
        propertyRemoteDataSource.saveProperties(fakeProperties).blockingAwait()
        Truth.assertThat(
            propertyRemoteDataSource.findAllProperties().blockingGet().size
        ).isEqualTo(fakeProperties.size)
        propertyRemoteDataSource.deleteAllProperties().blockingAwait()
        Truth.assertThat(propertyRemoteDataSource.findAllProperties().blockingGet()).isEmpty()
    }
}