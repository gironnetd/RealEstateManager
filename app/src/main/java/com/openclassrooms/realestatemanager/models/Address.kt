package com.openclassrooms.realestatemanager.models

import android.database.Cursor
import androidx.room.ColumnInfo

data class Address(
        var street: String = "",
        var city: String = "",
        @ColumnInfo(name = "postal_code")
        var postalCode: String = "",
        var country: String = "",
        var state: String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
) {
    constructor(cursor: Cursor): this() {
        street = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_STREET))
        city = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_CITY))
        postalCode = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_POSTAL_CODE))
        country = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_COUNTRY))
        state = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS_STATE))
        latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_ADDRESS_LATITUDE))
        longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_ADDRESS_LONGITUDE))
    }

    companion object {
        /** The name of the street column.  */
        const val COLUMN_ADDRESS_STREET = "street"

        /** The name of the city column.  */
        const val COLUMN_ADDRESS_CITY = "city"

        /** The name of the postal code column.  */
        const val COLUMN_ADDRESS_POSTAL_CODE = "postal_code"

        /** The name of the state column.  */
        const val COLUMN_ADDRESS_STATE = "state"

        /** The name of the country column.  */
        const val COLUMN_ADDRESS_COUNTRY = "country"

        /** The name of the latitude column.  */
        const val COLUMN_ADDRESS_LATITUDE = "latitude"

        /** The name of the longitude column.  */
        const val COLUMN_ADDRESS_LONGITUDE = "longitude"
    }

    override fun toString(): String {
        return "$street\n$city\n$postalCode\n$country\n$state"
    }
}
