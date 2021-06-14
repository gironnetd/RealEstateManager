package com.openclassrooms.realestatemanager.data.local.source

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.PropertyType
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyLocalDataSourceTest : TestCase() {

    private lateinit var database: AppDatabase
    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    private lateinit var localDataSource: PropertyLocalDataSource

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)

        localDataSource = PropertyLocalDataSource(database.propertyDao())
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun given_local_data_source_when_save_a_property_then_saved_successfully() {
        // Given properties list and When properties list saved
        localDataSource.saveProperty(fakeProperties[0]).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(localDataSource.count().blockingGet()).isEqualTo(1)
    }

    @Test
    fun given_local_data_source_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        localDataSource.saveProperties(fakeProperties).blockingAwait()

        // Then count of properties in database is equal to given properties list size
        assertThat(localDataSource.count().blockingGet()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_local_data_source_when_find_all_properties_then_found_successfully() {

        // Given properties list
        fakeProperties = fakeProperties.sortedBy { it.id }

        // When properties list saved
        localDataSource.saveProperties(fakeProperties).blockingAwait()

        var actualProperties = localDataSource.findAllProperties().blockingGet()

        // Then returned properties in database is equal to given properties list
        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    @Test
    fun given_local_data_source_when_find_property_by_id_then_found_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        val property = fakeProperties[fakeProperties.indices.random()]
        val expectedProperty: Property = localDataSource.findPropertyById(property.id).blockingGet()
        assertThat(expectedProperty).isEqualTo(property)
    }

    @Test
    fun given_local_data_source_when_find_properties_by_ids_then_found_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        val expectedProperties: List<Property> = localDataSource.findPropertiesByIds(propertyIds).blockingGet()
        assertThat(expectedProperties).isEqualTo(fakeProperties.subList(0, 2))
    }

    @Test
    fun given_local_data_source_when_update_property_then_updated_successfully() {
        val initialProperty = fakeProperties[fakeProperties.indices.random()]

        localDataSource.saveProperty(initialProperty).blockingAwait()

        val updatedProperty = initialProperty.copy()
        with(updatedProperty) {
            description = "new description"
            type = PropertyType.values().first { type -> type != initialProperty.type }
        }
        localDataSource.updateProperty(updatedProperty).blockingAwait()

        val finalProperty = localDataSource.findPropertyById(initialProperty.id).blockingGet()
        assertThat(finalProperty).isEqualTo(updatedProperty)
    }

    @Test
    fun given_local_data_source_when_update_properties_then_updated_successfully() {
        var initialProperties = Array(2) { fakeProperties[fakeProperties.indices.random()] }

        localDataSource.saveProperties(initialProperties.asList()).blockingAwait()

        var updatedProperties = initialProperties.copyOf().toList()
        updatedProperties.forEachIndexed { index,  updatedProperty ->
            with(updatedProperty) {
                description = "new description"
                type = PropertyType.values().first { type -> type != initialProperties[index].type }
            }
        }
        updatedProperties = updatedProperties.sortedBy { it.id }

        localDataSource.updateProperties(updatedProperties).blockingAwait()

        var finalProperties = localDataSource.findAllProperties().blockingGet()
        finalProperties = finalProperties.sortedBy { it.id }

        assertThat(finalProperties).isEqualTo(updatedProperties.toList())
    }

    @Test
    fun given_local_data_source_when_delete_property_by_id_then_deleted_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        val property = fakeProperties[fakeProperties.indices.random()]
        localDataSource.deleteById(property.id).blockingAwait()
        assertThat(localDataSource.findAllProperties().blockingGet().contains(property))
            .isFalse()
    }

    @Test
    fun given_local_data_source_when_delete_properties_by_ids_then_deleted_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        localDataSource.deletePropertiesByIds(propertyIds).blockingAwait()

        val findAllProperties = localDataSource.findAllProperties().blockingGet()
        assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
        assertThat(findAllProperties.containsAll(fakeProperties.subList(0, 2))).isFalse()
    }

    @Test
    fun given_local_data_source_when_delete_properties_then_deleted_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        assertThat(localDataSource.findAllProperties().blockingGet().size).isEqualTo(fakeProperties.size)

        localDataSource.deleteProperties(fakeProperties.subList(0, 2)).blockingAwait()

        val findAllProperties = localDataSource.findAllProperties().blockingGet()
        assertThat(findAllProperties.size).isEqualTo((fakeProperties.size - 2))
    }

    @Test
    fun given_local_data_source_when_delete_all_properties_then_deleted_successfully() {
        localDataSource.saveProperties(fakeProperties).blockingAwait()
        assertThat(
            localDataSource.findAllProperties().blockingGet().size
        ).isEqualTo(fakeProperties.size)
        localDataSource.deleteAllProperties().blockingAwait()
        assertThat(localDataSource.findAllProperties().blockingGet()).isEmpty()
    }
}