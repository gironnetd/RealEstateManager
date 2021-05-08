package com.openclassrooms.realestatemanager.data.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PropertyApiServiceTest : TestCase() {

    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    lateinit var apiService: PropertyApiService

    @Before
    fun initApiService() {
        var app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as BaseApplication

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()

        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = settings

        apiService = DefaultPropertyApiService(firestore = firestore)

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
        fakeProperties = fakeProperties.sortedBy { it.id }
    }

    @Test
    fun insert_properties_with_success() {
        apiService.insertProperties(properties = fakeProperties).blockingAwait()

        val actualProperties = apiService.findAllProperties().blockingGet()
        assertThat(actualProperties).isNotNull()
        assertThat(actualProperties.size).isEqualTo(fakeProperties.size)
    }

    @Test
    fun is_properties_after_insertion_are_the_same_when_reading_result() {
        apiService.insertProperties(properties = fakeProperties).blockingAwait()

        val actualProperties = apiService.findAllProperties().blockingGet()
        assertThat(actualProperties).isEqualTo(fakeProperties)
    }
}