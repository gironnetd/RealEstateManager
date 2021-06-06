package com.openclassrooms.realestatemanager.data.local.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import com.openclassrooms.realestatemanager.BaseApplication
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.models.Photo
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

    override fun onCreate(): Boolean {
        return true
    }

    @Inject
    lateinit var database: AppDatabase

    override fun getType(uri: Uri): String? {
        return when (sUriMatcher.match(uri)) {
            PROPERTY -> PropertyContract.PropertyEntry.CONTENT_TYPE
            PROPERTY_ID -> PropertyContract.PropertyEntry.CONTENT_ITEM_TYPE
            PHOTO -> PropertyContract.PhotoEntry.CONTENT_TYPE
            PHOTO_ID -> PropertyContract.PhotoEntry.CONTENT_ITEM_TYPE
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val retCursor: Cursor
        if(!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }

        when (sUriMatcher.match(uri)) {
            PROPERTY -> retCursor = database.propertyDao().findAllProperties()

            PROPERTY_ID -> {
                val _id = ContentUris.parseId(uri)
                retCursor = database.propertyDao().findPropertyById(_id)
            }
            PHOTO -> retCursor = database.photoDao().findAllPhotos()
            PHOTO_ID -> {
                val _id = ContentUris.parseId(uri)
                retCursor = database.photoDao().findPhotoById(_id)
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        // Set the notification URI for the cursor to the one passed into the function. This
        // causes the cursor to register a content observer to watch for changes that happen to
        // this URI and any of it's descendants. By descendants, we mean any URI that begins
        // with this path.
        retCursor.setNotificationUri(context!!.contentResolver, uri)
        return retCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val _id: Long
        val returnUri: Uri

        if(!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }
        when (sUriMatcher.match(uri)) {
            PROPERTY -> {
                _id = database.propertyDao().saveProperty(Property.fromContentValues(values))
                returnUri = if (_id > 0) {
                    PropertyContract.PropertyEntry.buildPropertyUri(_id)
                } else {
                    throw UnsupportedOperationException("Unable to insert rows into: $uri")
                }
            }
            PHOTO -> {
                _id = database.photoDao().savePhoto(Photo.fromContentValues(values))
                returnUri = if (_id > 0) {
                    PropertyContract.PhotoEntry.buildPhotoUri(_id)
                } else {
                    throw UnsupportedOperationException("Unable to insert rows into: $uri")
                }
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }

        // Use this on the URI passed into the function to notify any observers that the uri has
        // changed.
        context!!.contentResolver.notifyChange(uri, null)
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val context = context ?: return 0
        if (!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }

        val rows = when (sUriMatcher.match(uri)) {
            PROPERTY -> {
                database.propertyDao().deleteAllProperties()
            }
            PROPERTY_ID -> {
                database.propertyDao().deleteById(ContentUris.parseId(uri))
            }
            PHOTO -> {
                database.photoDao().deleteAllPhotos()
            }
            PHOTO_ID -> {
                database.photoDao().deleteById(ContentUris.parseId(uri))
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (rows != 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return rows
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val context = context ?: return 0
        if (!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }

        val rows = when (sUriMatcher.match(uri)) {
            PROPERTY -> {
                val property = Property.fromContentValues(values)
                database.propertyDao().updateProperty(property)
            }
            PHOTO -> {
                val photo: Photo = Photo.fromContentValues(values)
                database.photoDao().updatePhoto(photo)
            }
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (rows != 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return rows
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
        val context = context ?: return 0
        if (!::database.isInitialized) {
            (context as BaseApplication).appComponent.inject(this)
        }
        return when (sUriMatcher.match(uri)) {
            PROPERTY -> {
                val properties: Array<Property?> = arrayOfNulls(valuesArray.size)
                for (i in valuesArray.indices) {
                    properties[i] = Property.fromContentValues(valuesArray[i])
                }
                database.propertyDao().saveProperties(properties.toList() as List<Property>).size
            }
            PROPERTY_ID -> {
                val property: Property = Property.fromContentValues(valuesArray[0])
                database.propertyDao().saveProperty(property).toInt()
            }
            PHOTO -> {
                val photos: Array<Photo?> = arrayOfNulls(valuesArray.size)
                for (i in valuesArray.indices) {
                    photos[i] = Photo.fromContentValues(valuesArray[i])
                }
                database.photoDao().savePhotos(photos.toList() as List<Photo>).size
            }
            PHOTO_ID -> {
                val photo: Photo = Photo.fromContentValues(valuesArray[0])
                database.photoDao().savePhoto(photo).toInt()
            }
            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }
    }

    companion object {
        // Use an int for each URI we will run, this represents the different queries
        private const val PROPERTY = 100
        private const val PROPERTY_ID = 101
        private const val PHOTO = 200
        private const val PHOTO_ID = 201
        private val sUriMatcher = buildUriMatcher()

        /**
         * Builds a UriMatcher that is used to determine witch database request is being made.
         */
        private fun buildUriMatcher(): UriMatcher {
            val content: String = PropertyContract.CONTENT_AUTHORITY

            // All paths to the UriMatcher have a corresponding code to return
            // when a match is found (the ints above).
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(content, PropertyContract.PATH_PROPERTY, PROPERTY)
            matcher.addURI(content, PropertyContract.PATH_PROPERTY + "/#", PROPERTY_ID)
            matcher.addURI(content, PropertyContract.PATH_PHOTO, PHOTO)
            matcher.addURI(content, PropertyContract.PATH_PHOTO + "/#", PHOTO_ID)
            return matcher
        }
    }
}