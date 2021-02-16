package com.openclassrooms.realestatemanager.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyApiServiceTest : TestCase() {

    @Inject
    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    @Inject
    lateinit var apiService: PropertyApiService

    @Before
    fun initApiService() {
        var app : TestBaseApplication = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        injectTest(app)

        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.propertyId }
    }

    @Test
    fun insert_properties_with_success() {
        apiService.insertProperties(properties = fakeProperties).blockingGet()

        val actualProperties = apiService.findAllProperties().blockingFirst()
        assertThat(actualProperties).isNotNull()
        assertThat(actualProperties.size).isEqualTo(fakeProperties.size)
    }

    @Test
    fun is_properties_after_insertion_are_the_same_when_reading_result() {
        apiService.insertProperties(properties = fakeProperties).blockingAwait()

        val actualProperties = apiService.findAllProperties().blockingFirst()
        assertThat(actualProperties).isEqualTo(fakeProperties)
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}