package com.openclassrooms.realestatemanager.data.local.provider

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.local.provider.AppContentProvider.Companion.URI_PROPERTY
import com.openclassrooms.realestatemanager.models.*
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_CITY
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_COUNTRY
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_LATITUDE
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_LONGITUDE
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_POSTAL_CODE
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_STATE
import com.openclassrooms.realestatemanager.models.Address.Companion.COLUMN_ADDRESS_STREET
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_PICTURE_DESCRIPTION
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_PICTURE_PROPERTY_ID
import com.openclassrooms.realestatemanager.models.Picture.Companion.COLUMN_PICTURE_TYPE
import com.openclassrooms.realestatemanager.models.Picture.Companion.PREFIX_MAIN_PICTURE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_AGENT_ID
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_BATHROOMS
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_BEDROOMS
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_DESCRIPTION
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ENTRY_DATE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_INTEREST_POINTS
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PICTURES
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PRICE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PROPERTY_TYPE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ROOMS
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_SOLD_DATE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_SURFACE
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class AppContentProviderTest : TestCase() {

    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    private lateinit var mContentResolver: ContentResolver

    @Before
    fun initDatabase() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        mContentResolver = context.contentResolver

        jsonUtil = JsonUtil()

        var rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )

        rawJson =  jsonUtil.readJSONFromAsset(ConstantsTest.PICTURES_DATA_FILENAME)

        fakeProperties.forEachIndexed { index, property ->

            val pictures: List<Picture> = Gson().fromJson(rawJson, object : TypeToken<List<Picture>>() {}.type)
            pictures.forEach { picture ->
                picture.propertyId = property.id
            }

            fakeProperties[index].pictures.addAll(pictures)
        }
    }

    @Test
    fun properties_initially_empty() {
        val cursor = mContentResolver.query(URI_PROPERTY,
                arrayOf(COLUMN_ID), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(0)
        cursor.close()
    }

    @Test
    fun insert_a_property() {
        val itemUri = mContentResolver.insert(URI_PROPERTY,
                property(fakeProperties[0]))
        assertThat(itemUri).isNotNull()
        val cursor = mContentResolver.query(URI_PROPERTY, arrayOf(COLUMN_DESCRIPTION), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)))
                .isEqualTo(fakeProperties[0].description)
        cursor.close()
    }

    @Test
    fun properties_apply_batch() {
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValues(property(fakeProperties[0]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValues(property(fakeProperties[1]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValues(property(fakeProperties[2]))
                .build())
        val results = mContentResolver.applyBatch(
                AppContentProvider.AUTHORITY, operations)
        assertThat(results.size).isEqualTo(3)
        val cursor = mContentResolver.query(URI_PROPERTY, arrayOf(COLUMN_DESCRIPTION),
                null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        assertThat(cursor.moveToFirst()).isTrue()
        cursor.close()
    }

    @Test
    fun properties_bulk_insert() {
        val count = mContentResolver.bulkInsert(URI_PROPERTY,
                arrayOf(
                property(fakeProperties[0]),
                property(fakeProperties[1]),
                property(fakeProperties[2]))
        )
        assertThat(count).isEqualTo(3)
        val cursor = mContentResolver.query(URI_PROPERTY,
                arrayOf(COLUMN_DESCRIPTION), null,
                null,
                null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        cursor.close()
    }

    private fun property(fakeProperty: Property): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_ID, fakeProperty.id)
        values.put(COLUMN_PROPERTY_TYPE, fakeProperty.type.name)
        values.put(COLUMN_DESCRIPTION, fakeProperty.description)
        values.put(COLUMN_SURFACE, fakeProperty.surface)
        values.put(COLUMN_PRICE, fakeProperty.price)
        values.put(COLUMN_ROOMS, fakeProperty.rooms)
        values.put(COLUMN_BATHROOMS, fakeProperty.bathRooms)
        values.put(COLUMN_BEDROOMS, fakeProperty.bedRooms)
        values.put(COLUMN_INTEREST_POINTS, InterestPointConverter().interestPointsToString(fakeProperty.interestPoints))
        values.put(COLUMN_SURFACE, fakeProperty.surface)
        values.put(COLUMN_ADDRESS_STREET, fakeProperty.address!!.street)
        values.put(COLUMN_ADDRESS_CITY, fakeProperty.address!!.city)
        values.put(COLUMN_ADDRESS_POSTAL_CODE, fakeProperty.address!!.postalCode)
        values.put(COLUMN_ADDRESS_COUNTRY, fakeProperty.address!!.country)
        values.put(COLUMN_ADDRESS_STATE, fakeProperty.address!!.state)
        values.put(COLUMN_ADDRESS_LATITUDE, fakeProperty.address!!.latitude)
        values.put(COLUMN_ADDRESS_LONGITUDE, fakeProperty.address!!.longitude)
        values.put(COLUMN_AGENT_ID, fakeProperty.agentId)
        values.put(PREFIX_MAIN_PICTURE + Picture.COLUMN_ID, fakeProperty.mainPicture!!.id)
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID, fakeProperty.mainPicture!!.propertyId )
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION, fakeProperty.mainPicture!!.description)
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE, fakeProperty.mainPicture!!.type.name)
        values.put(COLUMN_ENTRY_DATE, DateConverter().dateToTimestamp(fakeProperty.entryDate))
        values.put(COLUMN_SOLD_DATE, DateConverter().dateToTimestamp(fakeProperty.soldDate))
        values.put(COLUMN_PICTURES, PictureConverter().picturesToString(fakeProperty.pictures))

        return values
    }
}