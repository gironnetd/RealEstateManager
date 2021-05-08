package com.openclassrooms.realestatemanager.data.local.provider

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.local.provider.PropertyContract.Companion.CONTENT_AUTHORITY
import com.openclassrooms.realestatemanager.data.local.provider.PropertyContract.PropertyEntry.Companion.CONTENT_URI
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
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PRICE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PROPERTY_TYPE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ROOMS
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_SOLD_DATE
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_SURFACE
import com.openclassrooms.realestatemanager.util.ConstantsTest.PICTURES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AppContentProviderTest : TestCase() {

    lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>
    private lateinit var fakePictures: List<Picture>

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

        rawJson =  jsonUtil.readJSONFromAsset(PICTURES_DATA_FILENAME)

        fakePictures = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Picture>>() {}.type
        )
    }

    @After
    fun clearDatabase() {
        mContentResolver.delete(CONTENT_URI, null, null)
        mContentResolver.delete(PropertyContract.PictureEntry.CONTENT_URI, null, null)
    }

    @Test
    fun properties_initially_empty() {
        val cursor = mContentResolver.query(CONTENT_URI,
                arrayOf(COLUMN_ID), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(0)
        cursor.close()
    }

    @Test
    fun insert_a_property() {
        val itemUri = mContentResolver.insert(CONTENT_URI,
                property(fakeProperties[0]))
        assertThat(itemUri).isNotNull()
        val cursor = mContentResolver.query(CONTENT_URI, arrayOf(COLUMN_DESCRIPTION), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)))
                .isEqualTo(fakeProperties[0].description)
        cursor.close()
    }

    @Test
    fun update_a_property() {
        val contentValues: ContentValues = property(fakeProperties[0])
        val itemUri = mContentResolver.insert(CONTENT_URI, contentValues)
        assertThat(itemUri).isNotNull()

        val newDescription = "New Description"
        val updatedContentValues = ContentValues(contentValues)
        updatedContentValues.put(COLUMN_DESCRIPTION, newDescription)

        mContentResolver.update(
                CONTENT_URI,
                updatedContentValues,
                "$COLUMN_ID = ?",
                arrayOf(updatedContentValues.getAsString(COLUMN_ID))
        )

        val returnedValue =  mContentResolver.query(
                CONTENT_URI,
                null,
                null,
                null,
                null)

        validateCursor(returnedValue!!, updatedContentValues)
    }

    @Test
    fun properties_apply_batch() {
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(ContentProviderOperation
                .newInsert(CONTENT_URI)
                .withValues(property(fakeProperties[0]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(CONTENT_URI)
                .withValues(property(fakeProperties[1]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(CONTENT_URI)
                .withValues(property(fakeProperties[2]))
                .build())
        val results = mContentResolver.applyBatch(
                CONTENT_AUTHORITY, operations)
        assertThat(results.size).isEqualTo(3)
        val cursor = mContentResolver.query(CONTENT_URI, arrayOf(COLUMN_DESCRIPTION),
                null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        assertThat(cursor.moveToFirst()).isTrue()
        cursor.close()
    }

    @Test
    fun properties_bulk_insert() {
        val count = mContentResolver.bulkInsert(CONTENT_URI,
                arrayOf(
                        property(fakeProperties[0]),
                        property(fakeProperties[1]),
                        property(fakeProperties[2]))
        )
        assertThat(count).isEqualTo(3)
        val cursor = mContentResolver.query(CONTENT_URI,
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
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID, fakeProperty.mainPicture!!.propertyId)
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION, fakeProperty.mainPicture!!.description)
        values.put(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE, fakeProperty.mainPicture!!.type.name)
        values.put(COLUMN_ENTRY_DATE, DateConverter().dateToTimestamp(fakeProperty.entryDate))
        values.put(COLUMN_SOLD_DATE, DateConverter().dateToTimestamp(fakeProperty.soldDate))

        return values
    }

    @Test
    fun pictures_initially_empty() {
        val cursor = mContentResolver.query(PropertyContract.PictureEntry.CONTENT_URI,
                arrayOf(Picture.COLUMN_ID), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(0)
        cursor.close()
    }

    @Test
    fun insert_a_picture() {
        val itemUri = mContentResolver.insert(PropertyContract.PictureEntry.CONTENT_URI,
                picture(fakePictures[0]))
        assertThat(itemUri).isNotNull()
        val cursor = mContentResolver.query(PropertyContract.PictureEntry.CONTENT_URI,
                arrayOf(Picture.COLUMN_ID,
                        COLUMN_PICTURE_PROPERTY_ID,
                        COLUMN_PICTURE_DESCRIPTION,
                        COLUMN_PICTURE_TYPE
                ), null,
                null,
                null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(Picture.COLUMN_ID)))
                .isEqualTo(fakePictures[0].id)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PICTURE_PROPERTY_ID)))
                .isEqualTo(fakePictures[0].propertyId)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PICTURE_DESCRIPTION)))
                .isEqualTo(fakePictures[0].description)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PICTURE_TYPE)))
                .isEqualTo(fakePictures[0].type.name)
        cursor.close()
    }

    @Test
    fun update_a_picture() {
        val contentValues: ContentValues = picture(fakePictures[0])
        val itemUri = mContentResolver.insert(PropertyContract.PictureEntry.CONTENT_URI, contentValues)
        assertThat(itemUri).isNotNull()

        val newDescription = "New Description"
        val updatedContentValues = ContentValues(contentValues)
        updatedContentValues.put(COLUMN_PICTURE_DESCRIPTION, newDescription)

        mContentResolver.update(
                PropertyContract.PictureEntry.CONTENT_URI,
                updatedContentValues,
                "$Picture.COLUMN_ID = ?",
                arrayOf(updatedContentValues.getAsString(Picture.COLUMN_ID))
        )

        val returnedValue =  mContentResolver.query(
                PropertyContract.PictureEntry.CONTENT_URI,
                null,
                null,
                null,
                null)

        validateCursor(returnedValue!!, updatedContentValues)
    }

    @Test
    fun pictures_apply_batch() {
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PictureEntry.CONTENT_URI)
                .withValues(picture(fakePictures[0]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PictureEntry.CONTENT_URI)
                .withValues(picture(fakePictures[1]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PictureEntry.CONTENT_URI)
                .withValues(picture(fakePictures[2]))
                .build())
        val results = mContentResolver.applyBatch(
                CONTENT_AUTHORITY, operations)
        assertThat(results.size).isEqualTo(3)
        val cursor = mContentResolver.query(PropertyContract.PictureEntry.CONTENT_URI,
                arrayOf(Picture.COLUMN_ID,
                        COLUMN_PICTURE_PROPERTY_ID,
                        COLUMN_PICTURE_DESCRIPTION,
                        COLUMN_PICTURE_TYPE
                ),
                null,
                null,
                null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        assertThat(cursor.moveToFirst()).isTrue()
        cursor.close()
    }

    @Test
    fun pictures_bulk_insert() {
        val count = mContentResolver.bulkInsert(PropertyContract.PictureEntry.CONTENT_URI,
                arrayOf(
                        picture(fakePictures[0]),
                        picture(fakePictures[1]),
                        picture(fakePictures[2]))
        )
        assertThat(count).isEqualTo(3)
        val cursor = mContentResolver.query(PropertyContract.PictureEntry.CONTENT_URI,
                arrayOf(Picture.COLUMN_ID,
                        COLUMN_PICTURE_PROPERTY_ID,
                        COLUMN_PICTURE_DESCRIPTION,
                        COLUMN_PICTURE_TYPE
                ),
                null,
                null,
                null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        cursor.close()
    }

    private fun picture(fakePicture: Picture): ContentValues {

        val values = ContentValues()
        values.put(Picture.COLUMN_ID, fakePicture.id)
        values.put(COLUMN_PICTURE_PROPERTY_ID, fakePicture.propertyId)
        values.put(COLUMN_PICTURE_DESCRIPTION, fakePicture.description)
        values.put(COLUMN_PICTURE_TYPE, PictureTypeConverter().fromPictureType(fakePicture.type))

        return values
    }

    private fun validateCursor(valueCursor: Cursor, expectedValues: ContentValues) {
        assertTrue(valueCursor.moveToFirst())
        val valueSet = expectedValues.valueSet()
        for ((columnName, value) in valueSet) {
            val idx = valueCursor.getColumnIndex(columnName)
            assertFalse(idx == -1)
            when (valueCursor.getType(idx)) {
                Cursor.FIELD_TYPE_FLOAT -> assertEquals(value, valueCursor.getDouble(idx))
                Cursor.FIELD_TYPE_INTEGER -> assertEquals(value.toString().toInt(), valueCursor.getInt(idx))
                Cursor.FIELD_TYPE_STRING -> assertEquals(value, valueCursor.getString(idx))
                Cursor.FIELD_TYPE_NULL -> {}
                else -> assertEquals(value.toString(), valueCursor.getString(idx))
            }
        }
        valueCursor.close()
    }
}