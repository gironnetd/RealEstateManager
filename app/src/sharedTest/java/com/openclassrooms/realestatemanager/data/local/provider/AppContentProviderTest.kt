package com.openclassrooms.realestatemanager.data.local.provider

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.TestBaseApplication
import com.openclassrooms.realestatemanager.data.local.provider.AppContentProvider.Companion.URI_PROPERTY
import com.openclassrooms.realestatemanager.di.TestAppComponent
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_DESCRIPTION
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_ID
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class AppContentProviderTest : TestCase() {

    @Inject lateinit var jsonUtil: JsonUtil
    private lateinit var fakeProperties: List<Property>

    private lateinit var mContentResolver: ContentResolver

    @Before
    fun initDatabase() {
        val app = InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .applicationContext as TestBaseApplication

        injectTest(app)

        val context = ApplicationProvider.getApplicationContext<Context>()
        mContentResolver = context.contentResolver

        val rawJson = jsonUtil.readJSONFromAsset(PROPERTIES_DATA_FILENAME)
        fakeProperties = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
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
                propertyWithDescription("1","test description"))
        assertThat(itemUri).isNotNull()
        val cursor = mContentResolver.query(URI_PROPERTY, arrayOf(COLUMN_DESCRIPTION), null, null, null)
        assertThat(cursor).isNotNull()
        assertThat(cursor!!.count).isEqualTo(1)
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)))
                .isEqualTo("test description")
        cursor.close()
    }

    @Test
    fun properties_apply_batch() {
        val operations = ArrayList<ContentProviderOperation>()
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValue(COLUMN_ID, "1")
                .withValue(COLUMN_DESCRIPTION, "test description 1")
                .build())
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValue(COLUMN_ID, "2")
                .withValue(COLUMN_DESCRIPTION, "test description 2")
                .build())
        operations.add(ContentProviderOperation
                .newInsert(URI_PROPERTY)
                .withValue(COLUMN_ID, "3")
                .withValue(COLUMN_DESCRIPTION, "test description 3")
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
                propertyWithDescription("1","test description 1"),
                propertyWithDescription("2","test description 2"),
                propertyWithDescription("3","test description 3"))
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

    private fun propertyWithDescription(id: String,description: String): ContentValues? {
        val values = ContentValues()
        values.put(COLUMN_ID, id)
        values.put(COLUMN_DESCRIPTION, description)
        return values
    }

    private fun injectTest(application: TestBaseApplication) {
        (application.appComponent as TestAppComponent)
                .inject(this)
    }
}