package com.openclassrooms.realestatemanager.data.local.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import androidx.annotation.Nullable
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.models.Property
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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
        TODO("Not yet implemented")
    }

    @Nullable
    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    @Nullable
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun bulkInsert(uri: Uri, valuesArray: Array<out ContentValues>): Int {
        TODO("Not yet implemented")
    }
}