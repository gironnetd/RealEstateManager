package com.openclassrooms.realestatemanager.models

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.models.Property.Companion.TABLE_NAME
import java.util.*

@Entity(tableName = TABLE_NAME)
data class Property(

        @PrimaryKey
        @ColumnInfo(index = true, name = COLUMN_ID)
        @SerializedName(value = "id")
        var propertyId: String = "",

        @SerializedName(value = "type")
        @ColumnInfo(name = "type")
        var propertyType: PropertyType = PropertyType.NONE,

        @ColumnInfo(name = "price")
        var price: Int = 0,

        @ColumnInfo(name = "surface")
        var surface: Int = 0,

        @ColumnInfo(name = "rooms")
        var rooms: Int = 0,

        @ColumnInfo(name = "bedrooms")
        var bedRooms: Int = 0,

        @ColumnInfo(name = "bathrooms")
        var bathRooms: Int = 0,

        @ColumnInfo(name = "description")
        var description: String = "",

        @Embedded
        var address: Address? = null,

        @ColumnInfo(name = "interest_points")
        var interestPoints: MutableList<InterestPoint> = mutableListOf(),
        var status: PropertyStatus = PropertyStatus.NONE,

        @ColumnInfo(name = "agent_id")
        var agentId: String? = null,

        @Embedded(prefix = Picture.PREFIX_MAIN_PICTURE)
        var mainPicture: Picture? = null,

        @ColumnInfo(name = "entry_date")
        var entryDate: Date = Date(),

        @ColumnInfo(name = "sold_date")
        var soldDate: Date? = null,
) {

        constructor(cursor: Cursor) : this() {
                propertyId = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
                propertyType = PropertyType.valueOf(
                        cursor.getString(cursor.getColumnIndex(COLUMN_PROPERTY_TYPE)))
                price = cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE))
                surface = cursor.getInt(cursor.getColumnIndex(COLUMN_SURFACE))
                rooms = cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMS))
                bedRooms = cursor.getInt(cursor.getColumnIndex(COLUMN_BEDROOMS))
                bathRooms = cursor.getInt(cursor.getColumnIndex(COLUMN_BATHROOMS))
                description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                address = Address(cursor = cursor)
                cursor.getString(cursor.getColumnIndex(COLUMN_INTEREST_POINTS))?.let {
                        interestPoints = InterestPointConverter().stringToInterestPoints(it)!!
                }
                status = PropertyStatus.valueOf(
                        cursor.getString(cursor.getColumnIndex(COLUMN_PROPERTY_STATUS)))
                agentId = cursor.getString(cursor.getColumnIndex(COLUMN_AGENT_ID))
                mainPicture = Picture(cursor = cursor, isMainPicture = true)
                if(!cursor.isNull(cursor.getColumnIndex(COLUMN_ENTRY_DATE))) {
                        entryDate = DateConverter()
                                .fromTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_ENTRY_DATE)))!!
                }
                if(!cursor.isNull(cursor.getColumnIndex(COLUMN_SOLD_DATE))) {
                        soldDate = DateConverter()
                                .fromTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_SOLD_DATE)))!!
                }

        }

        companion object {
                /** The name of the Property table.  */
                const val TABLE_NAME: String = "properties"

                /** The name of the ID column.  */
                const val COLUMN_ID: String = BaseColumns._ID

                /** The name of the property type column.  */
                //const val COLUMN_PROPERTY_ID = "id"

                /** The name of the property type column.  */
                const val COLUMN_PROPERTY_TYPE = "type"

                /** The name of the price column.  */
                const val COLUMN_PRICE = "price"

                /** The name of the surface column.  */
                const val COLUMN_SURFACE = "surface"

                /** The name of the rooms column.  */
                const val COLUMN_ROOMS = "rooms"

                /** The name of the bedrooms column.  */
                const val COLUMN_BEDROOMS = "bedrooms"

                /** The name of the bathrooms type column.  */
                const val COLUMN_BATHROOMS = "bathrooms"

                /** The name of the description column.  */
                const val COLUMN_DESCRIPTION = "description"

                /** The name of the address column.  */
                const val COLUMN_ADDRESS = "address"

                /** The name of the interest points column.  */
                const val COLUMN_INTEREST_POINTS = "interest_points"

                /** The name of the property status column.  */
                const val COLUMN_PROPERTY_STATUS = "status"

                /** The name of the agent id column.  */
                const val COLUMN_AGENT_ID = "agent_id"

                /** The name of the main picture column.  */
                const val COLUMN_MAIN_PICTURE = "main_picture"

                /** The name of the entry date column.  */
                const val COLUMN_ENTRY_DATE = "entry_date"

                /** The name of the sold date column.  */
                const val COLUMN_SOLD_DATE = "sold_date"

                @NonNull
                fun fromContentValues(values: ContentValues?): Property {
                        val property = Property()
                        values?.let {
                                if ( it.containsKey(COLUMN_ID)) {
                                        property.propertyId = it.getAsString(COLUMN_ID)
                                }
                                if (it.containsKey(COLUMN_PROPERTY_TYPE)) {
                                        property.propertyType = PropertyType.valueOf(it.getAsString(COLUMN_PROPERTY_TYPE))
                                }
                                if ( it.containsKey(COLUMN_PRICE)) {
                                        property.price = it.getAsInteger(COLUMN_PRICE)
                                }
                                if (it.containsKey(COLUMN_SURFACE)) {
                                        property.surface = it.getAsInteger(COLUMN_SURFACE)
                                }
                                if (it.containsKey(COLUMN_ROOMS)) {
                                        property.rooms = it.getAsInteger(COLUMN_ROOMS)
                                }
                                if (it.containsKey(COLUMN_BEDROOMS)) {
                                        property.bedRooms = it.getAsInteger(COLUMN_BEDROOMS)
                                }
                                if (it.containsKey(COLUMN_BATHROOMS)) {
                                        property.bathRooms = it.getAsInteger(COLUMN_BATHROOMS)
                                }
                                if (it.containsKey(COLUMN_DESCRIPTION)) {
                                        property.description = it.getAsString(COLUMN_DESCRIPTION)
                                }

                                if (it.containsKey(COLUMN_ADDRESS)) {
                                        // property.description = it.getAsString(COLUMN_ADDRESS)
                                }

                                if (it.containsKey(COLUMN_INTEREST_POINTS)) {
                                        //   property.description = it.getAsString(COLUMN_INTEREST_POINTS)
                                }

                                if (it.containsKey(COLUMN_PROPERTY_STATUS)) {
                                        property.status = PropertyStatus.valueOf(it.getAsString(COLUMN_PROPERTY_STATUS))
                                }

                                if (it.containsKey(COLUMN_AGENT_ID)) {
                                        property.agentId = it.getAsString(COLUMN_AGENT_ID)
                                }

                                if (it.containsKey(COLUMN_MAIN_PICTURE)) {
                                        //  property.description = it.getAsString(COLUMN_MAIN_PICTURE)
                                }

                                if (it.containsKey(COLUMN_ENTRY_DATE)) {
                                        // property.entryDate = it.getAsString(COLUMN_ENTRY_DATE)
                                }

                                if (it.containsKey(COLUMN_SOLD_DATE)) {
                                        //  property.description = it.getAsString(COLUMN_SOLD_DATE)
                                }
                        }
                        return property
                }
        }
}

class DateConverter {
        @TypeConverter
        fun fromTimestamp(value: Long?): Date? {
                return value?.let { Date(it) }
        }

        @TypeConverter
        fun dateToTimestamp(date: Date?): Long? {
                return date?.time
        }
}
