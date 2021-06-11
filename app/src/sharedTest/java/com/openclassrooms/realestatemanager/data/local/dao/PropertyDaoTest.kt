package com.openclassrooms.realestatemanager.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class PropertyDaoTest: TestCase() {

    private lateinit var database: AppDatabase
    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    private lateinit var propertyDao: PropertyDao

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)
        propertyDao = database.propertyDao()
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun given_property_dao_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        propertyDao.saveProperties(fakeProperties)

        // Then count of properties in database is equal to given properties list size
        assertThat(propertyDao.count()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_property_dao_when_find_all_properties_then_found_successfully() {

        // Given properties list
        fakeProperties = fakeProperties.sortedBy { it.id }

        // When properties list saved
        propertyDao.saveProperties(fakeProperties)

        var actualProperties = propertyDao.findAllProperties().toList { Property(it) }

        // Then returned properties in database is equal to given properties list
        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    @Test
    fun given_property_dao_when_find_property_by_id_then_found_successfully() {
        propertyDao.saveProperties(fakeProperties)
        val property = fakeProperties[fakeProperties.indices.random()]
        val expectedProperty: Property = propertyDao.findPropertyById(property.id).toList { Property(it) }.single()
        assertThat(expectedProperty).isEqualTo(property)
    }

    @Test
    fun given_property_dao_when_one_property_saved_then_saved_successfully() {
        // Given property and When property saved
        val savedProperty: Property = fakeProperties[fakeProperties.indices.random()]
        propertyDao.saveProperty(savedProperty)

        // Then result  of reading is equal to given properties list
        val actualProperty = propertyDao.findPropertyById(savedProperty.id).toList { Property(it) }
            .single()

        assertThat(actualProperty).isNotNull()
        assertThat(actualProperty).isEqualTo(savedProperty)
    }

    @Test
    fun given_property_dao_when_update_property_then_updated_successfully() {
        val initialProperty = fakeProperties[fakeProperties.indices.random()]

        propertyDao.saveProperty(initialProperty)

        val updatedProperty = initialProperty.copy()
        with(updatedProperty) {
            description = "new description"
            surface = 34000
            rooms = 4
            bathRooms = 1
            bedRooms = 4
        }
        propertyDao.updateProperty(updatedProperty)

        val finalProperty = propertyDao.findPropertyById(initialProperty.id).toList { Property(it) }.single()
        assertThat(finalProperty).isEqualTo(updatedProperty)
    }

    @Test
    fun given_property_dao_when_delete_property_by_id_then_deleted_successfully() {
        propertyDao.saveProperties(fakeProperties)
        val property = fakeProperties[fakeProperties.indices.random()]
        propertyDao.deleteById(property.id)
        assertThat(propertyDao.findAllProperties().toList { Property(it) }.contains(property)).isFalse()
    }

    @Test
    fun given_property_dao_when_delete_all_properties_then_deleted_successfully() {
        propertyDao.saveProperties(fakeProperties)
        assertThat(propertyDao.findAllProperties().toList { Property(it) }.size
        ).isEqualTo(fakeProperties.size)
        propertyDao.deleteAllProperties()
        assertThat(propertyDao.findAllProperties().toList { Property(it) }).isEmpty()
    }
}