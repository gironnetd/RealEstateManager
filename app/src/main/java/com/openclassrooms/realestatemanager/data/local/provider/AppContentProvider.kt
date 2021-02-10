package com.openclassrooms.realestatemanager.data.local.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import androidx.annotation.Nullable
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.dao.PropertyDao
import com.openclassrooms.realestatemanager.models.Property
import java.util.*
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

fun <T> Cursor.toList(block: (Cursor) -> T) : List<T> {
    return mutableListOf<T>().also { list ->
        if (moveToFirst()) {
            do {
                list.add(block.invoke(this))
            } while (moveToNext())
        }
    }
}

@Singleton
class AppContentProvider : ContentProvider() {

    @Inject
    lateinit var database: AppDatabase

    companion object {
        /** The authority of this content provider.  */
        val AUTHORITY = "com.openclassrooms.realestatemanager.provider"

        /** The URI for the Cheese table.  */
        val URI_PROPERTY = Uri.parse(
                "content://" + AUTHORITY + "/" + Property.TABLE_NAME)

        /** The match code for some items in the Cheese table.  */
        private val CODE_PROPERTY_DIR = 1

        /** The match code for an item in the Cheese table.  */
        private val CODE_PROPERTY_ITEM = 2

        /** The URI matcher.  */
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)
    }

    init {
        MATCHER.addURI(AUTHORITY, Property.TABLE_NAME, CODE_PROPERTY_DIR)
        MATCHER.addURI(AUTHORITY, Property.TABLE_NAME + "/*", CODE_PROPERTY_ITEM)
    }

    override fun onCreate(): Boolean {
        return true
    }

    @Nullable
    override fun query(
            uri: Uri, projection: Array<out String>?, selection: String?,
            selectionArgs: Array<out String>?, sortOrder: String?,
    ): Cursor? {
        val code: Int = MATCHER.match(uri)
        return if (code == CODE_PROPERTY_DIR || code == CODE_PROPERTY_ITEM) {
            val context = context ?: return null
            if(!::database.isInitialized) {
                    (context as BaseApplication).appComponent.inject(this)
            }
            val propertyDao: PropertyDao = database.propertyDao()
            val cursor: Cursor = if (code == CODE_PROPERTY_DIR) {
                propertyDao.findAllProperties()
            } else {
                propertyDao.findPropertyById(ContentUris.parseId(uri))
            }
            cursor.setNotificationUri(context.contentResolver, uri)
            cursor
        } else {
            throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Nullable
    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            CODE_PROPERTY_DIR -> "vnd.android.cursor.dir/" + AUTHORITY + "." + Property.TABLE_NAME
            CODE_PROPERTY_ITEM -> "vnd.android.cursor.item/" + AUTHORITY + "." + Property.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Nullable
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (MATCHER.match(uri)) {
            CODE_PROPERTY_DIR -> {
                val context = context ?: return null
                if (!::database.isInitialized) {
                      (context as BaseApplication).appComponent.inject(this)
                }
                val id: Long = database.propertyDao().insertProperty(Property.fromContentValues(values))
                context.contentResolver.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id)
            }
            CODE_PROPERTY_ITEM -> throw java.lang.IllegalArgumentException("Invalid URI, cannot insert with ID: $uri")
            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
            uri: Uri, values: ContentValues?, selection: String?,
            selectionArgs: Array<out String>?,
    ): Int {
        TODO("Not yet implemented")
    }

    override fun applyBatch(operations: ArrayList<ContentProviderOperation>): Array<out ContentProviderResult> {

        val context = context ?: return arrayOf()

        if(!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }
        return database.runInTransaction(Callable<Array<ContentProviderResult>>
        { super@AppContentProvider.applyBatch(operations) })
    }

    override fun bulkInsert(uri: Uri, valuesArray: Array<out ContentValues>): Int {
        return when (MATCHER.match(uri)) {
            CODE_PROPERTY_DIR -> {
                val context = context ?: return 0

                val properties: Array<Property?> = arrayOfNulls(valuesArray.size)
                for (i in valuesArray.indices) {
                    properties[i] = Property.fromContentValues(valuesArray[i])
                }
                if (!::database.isInitialized) {
                     (context as BaseApplication).appComponent.inject(this)
                }
                database.propertyDao().insertProperties(properties.toList() as List<Property>).size
            }
            CODE_PROPERTY_ITEM -> throw java.lang.IllegalArgumentException("Invalid URI, cannot insert with ID: $uri")
            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }
    }
}