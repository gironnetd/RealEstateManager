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
    fun given_property_dao_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        propertyDao.saveProperties(*fakeProperties.toTypedArray())

        // Then count of photos in database is equal to given photos list size
        assertThat(propertyDao.count()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun given_property_dao_when_save_a_property_then_saved_successfully() {
        // Given properties list and When properties list saved
        propertyDao.saveProperty(fakeProperties[0])

        // Then count of properties in database is equal to given properties list size
        assertThat(propertyDao.findPropertyById(fakeProperties[0].id).toList { Property(it) }.single())
            .isEqualTo(fakeProperties[0])
    }

    @Test
    fun given_property_dao_when_save_properties_then_saved_successfully() {
        // Given properties list and When properties list saved
        fakeProperties = fakeProperties.sortedBy { it.id }
        propertyDao.saveProperties(*fakeProperties.toTypedArray())

        // Then count of properties in database is equal to given properties list size
        var actualProperties = propertyDao.findAllProperties().toList { Property(it) }

        actualProperties = actualProperties.sortedBy { it.id }
        assertThat(actualProperties).isEqualTo(fakeProperties)
    }

    @Test
    fun given_property_dao_when_find_all_properties_then_found_successfully() {

        // Given properties list
        fakeProperties = fakeProperties.sortedBy { it.id }

        // When properties list saved
        propertyDao.saveProperties(*fakeProperties.toTypedArray())

        var actualProperties = propertyDao.findAllProperties().toList { Property(it) }

        // Then returned properties in database is equal to given properties list
        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    @Test
    fun given_property_dao_when_find_property_by_id_then_found_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        val property = fakeProperties[fakeProperties.indices.random()]
        val expectedProperty: Property = propertyDao.findPropertyById(property.id).toList { Property(it) }.single()
        assertThat(expectedProperty).isEqualTo(property)
    }

    @Test
    fun given_property_dao_when_find_properties_by_ids_then_found_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        val expectedProperties: List<Property> = propertyDao.findPropertiesByIds(propertyIds)
        assertThat(expectedProperties).isEqualTo(fakeProperties.subList(0, 2))
    }

    @Test
    fun given_property_dao_when_update_property_then_updated_successfully() {
        val initialProperty = fakeProperties[fakeProperties.indices.random()]

        propertyDao.saveProperties(*fakeProperties.toTypedArray())

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
    fun given_property_dao_when_update_properties_then_updated_successfully() {
        var initialProperties = arrayOf(fakeProperties[0], fakeProperties[1])

        propertyDao.saveProperties(*fakeProperties.toTypedArray())

        var updatedProperties = initialProperties.copyOf().toList()
        updatedProperties.forEachIndexed { index,  updatedProperty ->
            with(updatedProperty) {
                description = "new description"
                type = com.openclassrooms.realestatemanager.models.PropertyType.values().first { type -> type != initialProperties[index].type }
            }
        }
        updatedProperties = updatedProperties.sortedBy { it.id }
        propertyDao.updateProperties(*updatedProperties.toTypedArray())

        val ids = initialProperties.map { photo -> photo.id }
        var finalProperties = propertyDao.findAllProperties().toList { Property(it) }.filter {
                photo -> ids.contains(photo.id)
        }
        finalProperties = finalProperties.sortedBy { it.id }

        assertThat(finalProperties).isEqualTo(updatedProperties.toList())
    }

    @Test
    fun given_property_dao_when_delete_property_by_id_then_deleted_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        val property = fakeProperties[fakeProperties.indices.random()]
        propertyDao.deletePropertyById(property.id)
        assertThat(propertyDao.findAllProperties().toList { Property(it) }.contains(property))
            .isFalse()
    }

    @Test
    fun given_property_dao_when_delete_properties_by_ids_then_deleted_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        val propertyIds = fakeProperties.subList(0, 2).map { property -> property.id }
        propertyDao.deletePropertiesByIds(propertyIds)

        val findAllProperties = propertyDao.findAllProperties()
        assertThat(findAllProperties.toList { Property(it) }.size).isEqualTo((fakeProperties.size - 2))
        assertThat(findAllProperties.toList { Property(it) }.containsAll(fakeProperties.subList(0, 2))).isFalse()
    }

    @Test
    fun given_property_dao_when_delete_properties_then_deleted_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        assertThat(propertyDao.findAllProperties().toList { Property(it) }.size).isEqualTo(fakeProperties.size)

        propertyDao.deleteProperties(*fakeProperties.subList(0, 2).toTypedArray())

        val findAllProperties = propertyDao.findAllProperties()
        assertThat(findAllProperties.toList { Property(it) }.size).isEqualTo((fakeProperties.size - 2))
    }

    @Test
    fun given_property_dao_when_delete_all_properties_then_deleted_successfully() {
        propertyDao.saveProperties(*fakeProperties.toTypedArray())
        assertThat(
            propertyDao.findAllProperties().toList { Property(it) }.size
        ).isEqualTo(fakeProperties.size)
        propertyDao.deleteAllProperties()
        assertThat(propertyDao.findAllProperties().toList { Property(it) }).isEmpty()
    }
}