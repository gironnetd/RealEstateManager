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
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_PHOTO_DESCRIPTION
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_PHOTO_PROPERTY_ID
import com.openclassrooms.realestatemanager.models.Photo.Companion.COLUMN_PHOTO_TYPE
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
import com.openclassrooms.realestatemanager.util.ConstantsTest.PHOTOS_DATA_FILENAME
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
    private lateinit var fakePhotos: List<Photo>

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

        rawJson =  jsonUtil.readJSONFromAsset(PHOTOS_DATA_FILENAME)

        fakePhotos = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Photo>>() {}.type
        )
    }

    @After
    fun clearDatabase() {
        mContentResolver.delete(CONTENT_URI, null, null)
        mContentResolver.delete(PropertyContract.PhotoEntry.CONTENT_URI, null, null)
    }

    @Test
    fun given_properties_empty_when_query_all_then_return_empty_result() {
        // Given properties list
        // When properties list is empty
        val cursor = mContentResolver.query(CONTENT_URI,
                arrayOf(COLUMN_ID), null, null, null)

        // Then returned query result is equal to zero
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(0)
        cursor.close()
    }

    @Test
    fun given_property_when_saved_then_query_return_property() {
        // Given property
        // When property is saved
        val itemUri = mContentResolver.insert(CONTENT_URI,
                property(fakeProperties[0]))
        assertThat(itemUri).isNotNull()

        // Then returned query result is equal to property saved
        val cursor = mContentResolver.query(CONTENT_URI, arrayOf(COLUMN_DESCRIPTION), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)))
                .isEqualTo(fakeProperties[0].description)
        cursor.close()
    }

    @Test
    fun given_property_when_update_it_then_query_return_property_updated() {
        // Given property
        val contentValues: ContentValues = property(fakeProperties[0])
        val itemUri = mContentResolver.insert(CONTENT_URI, contentValues)
        assertThat(itemUri).isNotNull()

        val newDescription = "New Description"
        val updatedContentValues = ContentValues(contentValues)
        updatedContentValues.put(COLUMN_DESCRIPTION, newDescription)

        // When property is updated
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

        // Then returned query result is equal to property updated
        validateCursor(returnedValue!!, updatedContentValues)
    }

    @Test
    fun given_properties_when_apply_batch_then_query_return_properties() {
        // Given properties
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

        // When apply batch on properties
        val results = mContentResolver.applyBatch(
                CONTENT_AUTHORITY, operations)
        assertThat(results.size).isEqualTo(3)

        // Then returned query result is equal to properties batched
        val cursor = mContentResolver.query(CONTENT_URI, arrayOf(COLUMN_DESCRIPTION),
                null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        assertThat(cursor.moveToFirst()).isTrue()
        cursor.close()
    }

    @Test
    fun given_properties_when_bulk_insert_then_query_return_properties() {
        // Given properties
        // When bulk insert on properties
        val count = mContentResolver.bulkInsert(CONTENT_URI,
                arrayOf(
                        property(fakeProperties[0]),
                        property(fakeProperties[1]),
                        property(fakeProperties[2]))
        )
        assertThat(count).isEqualTo(3)

        // Then returned query result is equal to properties bulked
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
//        values.put(PREFIX_MAIN_PHOTO + Photo.COLUMN_ID, fakeProperty.mainPhoto!!.id)
//        values.put(PREFIX_MAIN_PHOTO + COLUMN_PHOTO_PROPERTY_ID, fakeProperty.mainPhoto!!.propertyId)
//        values.put(PREFIX_MAIN_PHOTO + COLUMN_PHOTO_DESCRIPTION, fakeProperty.mainPhoto!!.description)
//        values.put(PREFIX_MAIN_PHOTO + COLUMN_PHOTO_TYPE, fakeProperty.mainPhoto!!.type.name)
        values.put(COLUMN_ENTRY_DATE, DateConverter().dateToTimestamp(fakeProperty.entryDate))
        values.put(COLUMN_SOLD_DATE, DateConverter().dateToTimestamp(fakeProperty.soldDate))

        return values
    }

    @Test
    fun given_photos_empty_when_query_all_then_return_empty_result() {
        // Given photos list
        // When photos list is empty
        val cursor = mContentResolver.query(PropertyContract.PhotoEntry.CONTENT_URI,
                arrayOf(Photo.COLUMN_ID), null, null, null)

        // Then returned query result is equal to zero
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(0)
        cursor.close()
    }

    @Test
    fun given_photo_when_saved_then_query_return_photo() {
        // Given photo
        // When photo  is saved
        val itemUri = mContentResolver.insert(PropertyContract.PhotoEntry.CONTENT_URI,
            photo(fakePhotos[0]))
        assertThat(itemUri).isNotNull()
        val cursor = mContentResolver.query(PropertyContract.PhotoEntry.CONTENT_URI,
                arrayOf(Photo.COLUMN_ID,
                        COLUMN_PHOTO_PROPERTY_ID,
                        COLUMN_PHOTO_DESCRIPTION,
                        COLUMN_PHOTO_TYPE
                ), null,
                null,
                null)

        // Then returned query result is equal to property saved
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(Photo.COLUMN_ID)))
                .isEqualTo(fakePhotos[0].id)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PROPERTY_ID)))
                .isEqualTo(fakePhotos[0].propertyId)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_DESCRIPTION)))
                .isEqualTo(fakePhotos[0].description)
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_TYPE)))
                .isEqualTo(fakePhotos[0].type.name)
        cursor.close()
    }

    @Test
    fun given_photo_when_update_it_then_query_return_photo_updated() {
        // Given photo
        val contentValues: ContentValues = photo(fakePhotos[0])
        val itemUri = mContentResolver.insert(PropertyContract.PhotoEntry.CONTENT_URI, contentValues)
        assertThat(itemUri).isNotNull()

        val newDescription = "New Description"
        val updatedContentValues = ContentValues(contentValues)
        updatedContentValues.put(COLUMN_PHOTO_DESCRIPTION, newDescription)

        // When photo is updated
        mContentResolver.update(
                PropertyContract.PhotoEntry.CONTENT_URI,
                updatedContentValues,
                "$Photo.COLUMN_ID = ?",
                arrayOf(updatedContentValues.getAsString(Photo.COLUMN_ID))
        )

        val returnedValue =  mContentResolver.query(
                PropertyContract.PhotoEntry.CONTENT_URI,
                null,
                null,
                null,
                null)

        // Then returned query result is equal to photo updated
        validateCursor(returnedValue!!, updatedContentValues)
    }

    @Test
    fun given_photos_when_apply_batch_then_query_return_photos() {
        // Given photos
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PhotoEntry.CONTENT_URI)
                .withValues(photo(fakePhotos[0]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PhotoEntry.CONTENT_URI)
                .withValues(photo(fakePhotos[1]))
                .build())
        operations.add(ContentProviderOperation
                .newInsert(PropertyContract.PhotoEntry.CONTENT_URI)
                .withValues(photo(fakePhotos[2]))
                .build())

        // When apply batch on photos
        val results = mContentResolver.applyBatch(
                CONTENT_AUTHORITY, operations)
        assertThat(results.size).isEqualTo(3)

        // Then returned query result is equal to photos batched
        val cursor = mContentResolver.query(PropertyContract.PhotoEntry.CONTENT_URI,
                arrayOf(Photo.COLUMN_ID,
                        COLUMN_PHOTO_PROPERTY_ID,
                        COLUMN_PHOTO_DESCRIPTION,
                        COLUMN_PHOTO_TYPE
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
    fun given_photos_when_bulk_insert_then_query_return_photos() {
        // Given photos
        // When bulk insert on photos
        val count = mContentResolver.bulkInsert(PropertyContract.PhotoEntry.CONTENT_URI,
                arrayOf(
                    photo(fakePhotos[0]),
                    photo(fakePhotos[1]),
                    photo(fakePhotos[2]))
        )
        assertThat(count).isEqualTo(3)

        // Then returned query result is equal to photos bulked
        val cursor = mContentResolver.query(PropertyContract.PhotoEntry.CONTENT_URI,
                arrayOf(Photo.COLUMN_ID,
                        COLUMN_PHOTO_PROPERTY_ID,
                        COLUMN_PHOTO_DESCRIPTION,
                        COLUMN_PHOTO_TYPE
                ), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(3)
        cursor.close()
    }

    private fun photo(fakePhoto: Photo): ContentValues {
        val values = ContentValues()
        values.put(Photo.COLUMN_ID, fakePhoto.id)
        values.put(COLUMN_PHOTO_PROPERTY_ID, fakePhoto.propertyId)
        values.put(COLUMN_PHOTO_DESCRIPTION, fakePhoto.description)
        values.put(COLUMN_PHOTO_TYPE, PhotoTypeConverter().fromPhotoType(fakePhoto.type))

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