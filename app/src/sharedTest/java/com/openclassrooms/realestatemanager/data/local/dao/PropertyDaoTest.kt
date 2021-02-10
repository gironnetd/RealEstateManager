package com.openclassrooms.realestatemanager.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class PropertyDaoTest: TestCase() {

    @Inject
    lateinit var database: AppDatabase
    private lateinit var propertyDao: PropertyDao

    @Inject
    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    @Before
    fun initDatabase() {
        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        injectTest(app)

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
        propertyDao.insertProperties(fakeProperties)
        assertThat(propertyDao.count()).isEqualTo(fakeProperties.size)
    }

    @Test
    fun is_properties_after_insertion_are_the_same_when_reading_result() {
        fakeProperties.sortedBy { it.propertyId }
        propertyDao.insertProperties(fakeProperties)
        val cursor = propertyDao.findAllProperties()

        val actualProperties = cursor.toList {
            Property(it)
        }

        actualProperties.sortedBy { it.propertyId }
        actualProperties.forEachIndexed { index, property ->
            assertThat(property).isEqualTo(fakeProperties[index])
        }
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}