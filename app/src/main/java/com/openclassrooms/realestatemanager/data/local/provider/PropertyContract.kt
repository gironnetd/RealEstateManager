package com.openclassrooms.realestatemanager.data.local.provider

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

class PropertyContract {

    companion object {
        /**
         * The Content Authority is a name for the entire content provider, similar to the relationship
         * between a domain name and its website. A convenient string to use for content authority is
         * the package name for the app, since it is guaranteed to be unique on the device.
         */
        const val CONTENT_AUTHORITY = "com.openclassrooms.realestatemanager.provider"

        /**
         * The content authority is used to create the base of all URIs which apps will use to
         * contact this content provider.
         */
        private val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")

        /**
         * A list of possible paths that will be appended to the base URI for each of the different
         * tables.
         */
        const val PATH_PROPERTY = "property"
        const val PATH_PHOTO = "photo"
    }

    /**
     * Create one class for each table that handles all information regarding the table schema and
     * the URIs related to it.
     */
    class PropertyEntry : BaseColumns {
        companion object {
            // Content URI represents the base location for the table
            val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROPERTY).build()

            // These are special type prefixes that specify if a URI returns a list or a specific item
            val CONTENT_TYPE = "vnd.android.cursor.dir/$CONTENT_URI/$PATH_PROPERTY"
            val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/$CONTENT_URI/$PATH_PROPERTY"

            // Define a function to build a URI to find a specific movie by it's identifier
            fun buildPropertyUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }
    }

    /**
     * Create one class for each table that handles all information regarding the table schema and
     * the URIs related to it.
     */
    class PhotoEntry : BaseColumns {

        companion object {
            // Content URI represents the base location for the table
            val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO).build()

            // These are special type prefixes that specify if a URI returns a list or a specific item
            val CONTENT_TYPE = "vnd.android.cursor.dir/$CONTENT_URI/$PATH_PHOTO"
            val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/$CONTENT_URI/$PATH_PHOTO"

            // Define a function to build a URI to find a specific movie by it's identifier
            fun buildPhotoUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }
    }
}