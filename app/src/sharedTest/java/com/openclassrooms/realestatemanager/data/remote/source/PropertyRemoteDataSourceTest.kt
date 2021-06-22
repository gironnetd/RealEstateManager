package com.openclassrooms.realestatemanager.data.remote.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.remote.firestore.PropertyFirestoreFeature
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_EMULATOR_HOST
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_FIRESTORE_PORT
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

    private lateinit var propertyFirestore: PropertyFirestoreFeature
    private lateinit var remoteDataSource: PropertyRemoteDataSource

    @Before
    fun initApiService() {
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false)
            .build()

        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_FIRESTORE_PORT)
        firestore.firestoreSettings = settings

        propertyFirestore = PropertyFirestoreFeature(firestore)
        remoteDataSource = PropertyRemoteDataSource(propertyFirestore = propertyFirestore)

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
    fun given_remote_data_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(remoteDataSource.count().blockingGet()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_remote_data_source_when_save_a_property_then_saved_successfully() {
        // Given properties list and When properties list saved
        remoteDataSource.saveProperty(fakeProperties[0]).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(remoteDataSource.findPropertyById(fakeProperties[0].id).blockingGet()).isEqualTo(fakeProperties[0])
    }

    @Test
    fun given_remote_data_source_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(remoteDataSource.findAllProperties().blockingGet()).isEqualTo(fakeProperties)
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

        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

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
        var initialProperties = arrayOf(fakeProperties[0], fakeProperties[1])

        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        var updatedProperties = initialProperties.copyOf().toList()
        updatedProperties.forEachIndexed { index,  updatedProperty ->
            with(updatedProperty) {
                description = "new description"
                type = com.openclassrooms.realestatemanager.models.PropertyType.values().first { type -> type != initialProperties[index].type }
            }
        }
        updatedProperties = updatedProperties.sortedBy { it.id }
        remoteDataSource.updateProperties(updatedProperties).blockingAwait()

        val ids = initialProperties.map { photo -> photo.id }
        var finalProperties = remoteDataSource.findAllProperties().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }
        finalProperties = finalProperties.sortedBy { it.id }

        assertThat(finalProperties).isEqualTo(updatedProperties.toList())
    }

    @Test
    fun given_remote_data_source_when_delete_property_by_id_then_deleted_successfully() {
        remoteDataSource.saveProperties(fakeProperties).blockingAwait()

        assertThat(remoteDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)
        val property = fakeProperties[fakeProperties.indices.random()]
        remoteDataSource.deletePropertyById(property.id).blockingAwait()
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