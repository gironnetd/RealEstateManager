package com.openclassrooms.realestatemanager.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.BaseApplication
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

    lateinit var database: AppDatabase
    private lateinit var propertyDao: PropertyDao
    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    @Before
    fun initDatabase() {
        var app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as BaseApplication

        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )

        propertyDao = database.propertyDao()
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun insert_properties_with_success() {
        propertyDao.saveProperties(fakeProperties)
        assertThat(propertyDao.count()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun is_properties_after_insertion_are_the_same_when_reading_result() {
        fakeProperties = fakeProperties.sortedBy { it.id }
        propertyDao.saveProperties(fakeProperties)
        val cursor = propertyDao.findAllProperties()

        var actualProperties = cursor.toList {
            Property(it)
        }

        actualProperties = actualProperties.sortedBy { it.id }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }
}