package com.openclassrooms.realestatemanager.models

import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.SerializedName
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
import com.openclassrooms.realestatemanager.models.Property.Companion.TABLE_NAME
import java.util.*

@Entity(tableName = TABLE_NAME)
data class Property(

        @PrimaryKey
        @ColumnInfo(index = true, name = COLUMN_ID)
        @SerializedName(value = "id")
        var id: String = "",

        @SerializedName(value = "type")
        @ColumnInfo(name = "type")
        var type: PropertyType = PropertyType.NONE,

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

        @Embedded(prefix = PREFIX_MAIN_PICTURE)
        var mainPicture: Picture? = null,

        @ColumnInfo(name = "entry_date")
        var entryDate: Date = Date(),

        @ColumnInfo(name = "sold_date")
        var soldDate: Date? = null,

        @Ignore
        var pictures: MutableList<Picture> = mutableListOf()
) {

        constructor(cursor: Cursor) : this() {
                id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
                type = PropertyType.valueOf(
                        cursor.getString(cursor.getColumnIndex(COLUMN_PROPERTY_TYPE)))
                price = cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE))
                surface = cursor.getInt(cursor.getColumnIndex(COLUMN_SURFACE))
                rooms = cursor.getInt(cursor.getColumnIndex(COLUMN_ROOMS))
                bedRooms = cursor.getInt(cursor.getColumnIndex(COLUMN_BEDROOMS))
                bathRooms = cursor.getInt(cursor.getColumnIndex(COLUMN_BATHROOMS))
                description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                if (!cursor.isNull(cursor.getColumnIndex(COLUMN_ADDRESS_STREET))) {
                        address = Address(cursor = cursor)
                }

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
                if(!cursor.isNull(cursor.getColumnIndex(COLUMN_PICTURES))) {
                        pictures = PictureConverter().stringToPictures(cursor.getString(cursor.getColumnIndex(COLUMN_PICTURES)))!!
                }
        }

        companion object {
                /** The name of the Property table.  */
                const val TABLE_NAME: String = "properties"

                /** The name of the ID column.  */
                const val COLUMN_ID: String = BaseColumns._ID

                /** The name of the property ID column for firestore.  */
                const val COLUMN_PROPERTY_ID = "id"

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

                /** The name of the pictures column.  */
                const val COLUMN_PICTURES = "pictures"

                fun cursorFromProperties(properties: List<Property>): Cursor {
                        val cursor = MatrixCursor(arrayOf(
                                COLUMN_ID,
                                COLUMN_PROPERTY_TYPE,
                                COLUMN_PRICE,
                                COLUMN_SURFACE,
                                COLUMN_ROOMS,
                                COLUMN_BEDROOMS,
                                COLUMN_BATHROOMS,
                                COLUMN_DESCRIPTION,
                                COLUMN_ADDRESS_STREET,
                                COLUMN_ADDRESS_CITY,
                                COLUMN_ADDRESS_POSTAL_CODE,
                                COLUMN_ADDRESS_COUNTRY,
                                COLUMN_ADDRESS_STATE,
                                COLUMN_ADDRESS_LATITUDE,
                                COLUMN_ADDRESS_LONGITUDE,
                                COLUMN_INTEREST_POINTS,
                                COLUMN_PROPERTY_STATUS,
                                COLUMN_AGENT_ID,
                                PREFIX_MAIN_PICTURE + Picture.COLUMN_ID,
                                PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID,
                                PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION,
                                PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE,
                                COLUMN_ENTRY_DATE,
                                COLUMN_SOLD_DATE,
                                COLUMN_PICTURES
                        ))

                        properties.forEach { property ->
                                cursor.newRow()
                                        .add(COLUMN_ID, property.id)
                                        .add(COLUMN_PROPERTY_TYPE, property.type.name)
                                        .add(COLUMN_PRICE, property.price)
                                        .add(COLUMN_SURFACE, property.surface)
                                        .add(COLUMN_ROOMS, property.rooms)
                                        .add(COLUMN_BEDROOMS, property.bedRooms)
                                        .add(COLUMN_BATHROOMS, property.bathRooms)
                                        .add(COLUMN_DESCRIPTION, property.description)
                                        .add(COLUMN_ADDRESS_STREET, property.address?.street)
                                        .add(COLUMN_ADDRESS_CITY, property.address?.city)
                                        .add(COLUMN_ADDRESS_POSTAL_CODE, property.address?.postalCode)
                                        .add(COLUMN_ADDRESS_COUNTRY, property.address?.country)
                                        .add(COLUMN_ADDRESS_STATE, property.address?.state)
                                        .add(COLUMN_ADDRESS_LATITUDE, property.address?.latitude)
                                        .add(COLUMN_ADDRESS_LONGITUDE, property.address?.longitude)
                                        .add(COLUMN_INTEREST_POINTS, InterestPointConverter().interestPointsToString(property.interestPoints))
                                        .add(COLUMN_PROPERTY_STATUS, property.status.name)
                                        .add(COLUMN_AGENT_ID, property.agentId)
                                        .add(PREFIX_MAIN_PICTURE + Picture.COLUMN_ID, property.mainPicture?.id )
                                        .add(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID, property.mainPicture?.propertyId)
                                        .add(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION, property.mainPicture?.description)
                                        .add(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE, property.mainPicture?.type?.name)
                                        .add(COLUMN_ENTRY_DATE, DateConverter().dateToTimestamp(property.entryDate))
                                        .add(COLUMN_SOLD_DATE, DateConverter().dateToTimestamp(property.soldDate))
                                        .add(COLUMN_PICTURES, PictureConverter().picturesToString(property.pictures))

                        }
                        return cursor
                }

                @NonNull
                fun fromContentValues(values: ContentValues?): Property {
                        val property = Property()
                        values?.let {
                                if ( it.containsKey(COLUMN_ID)) {
                                        property.id = it.getAsString(COLUMN_ID)
                                }
                                if (it.containsKey(COLUMN_PROPERTY_TYPE)) {
                                        property.type = PropertyType.valueOf(it.getAsString(COLUMN_PROPERTY_TYPE))
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

                                if(property.address == null) {
                                        property.address = Address()
                                }

                                if (it.containsKey(COLUMN_ADDRESS_STREET)) {
                                         property.address!!.street = it.getAsString(COLUMN_ADDRESS_STREET)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_CITY)) {
                                        property.address!!.city = it.getAsString(COLUMN_ADDRESS_CITY)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_POSTAL_CODE)) {
                                        property.address!!.postalCode = it.getAsString(COLUMN_ADDRESS_POSTAL_CODE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_COUNTRY)) {
                                        property.address!!.country = it.getAsString(COLUMN_ADDRESS_COUNTRY)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_STATE)) {
                                        property.address!!.state = it.getAsString(COLUMN_ADDRESS_STATE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_LATITUDE)) {
                                        property.address!!.latitude = it.getAsDouble(COLUMN_ADDRESS_LATITUDE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_LONGITUDE)) {
                                        property.address!!.longitude = it.getAsDouble(COLUMN_ADDRESS_LONGITUDE)
                                }

                                if (it.containsKey(COLUMN_INTEREST_POINTS)) {
                                        property.interestPoints = InterestPointConverter().stringToInterestPoints(it.getAsString(COLUMN_INTEREST_POINTS))!!
                                }

                                if (it.containsKey(COLUMN_PROPERTY_STATUS)) {
                                        property.status = PropertyStatus.valueOf(it.getAsString(COLUMN_PROPERTY_STATUS))
                                }

                                if (it.containsKey(COLUMN_AGENT_ID)) {
                                        property.agentId = it.getAsString(COLUMN_AGENT_ID)
                                }

                                if(property.mainPicture == null) {
                                        property.mainPicture = Picture()
                                }

                                if (it.containsKey(PREFIX_MAIN_PICTURE + Picture.COLUMN_ID)) {
                                          property.mainPicture!!.id = it.getAsString(PREFIX_MAIN_PICTURE + Picture.COLUMN_ID)
                                }

                                if (it.containsKey(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID)) {
                                        property.mainPicture!!.propertyId = it.getAsString(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_PROPERTY_ID)

                                }

                                if (it.containsKey(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION)) {
                                        property.mainPicture!!.description = it.getAsString(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_DESCRIPTION)
                                }

                                if (it.containsKey(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE)) {
                                        property.mainPicture!!.type = PictureTypeConverter()
                                                .toPictureType(it.getAsString(PREFIX_MAIN_PICTURE + COLUMN_PICTURE_TYPE))
                                }

                                if (it.containsKey(COLUMN_ENTRY_DATE)) {
                                         property.entryDate = DateConverter()
                                                 .fromTimestamp(it.getAsLong(COLUMN_ENTRY_DATE))!!
                                }

                                if (it.containsKey(COLUMN_SOLD_DATE)) {
                                        it.getAsLong(COLUMN_SOLD_DATE)?.let { soldDate ->
                                                property.entryDate = DateConverter()
                                                        .fromTimestamp(soldDate)!!
                                        }
                                }
                                if(it.containsKey(COLUMN_PICTURES)) {
                                        property.pictures = PictureConverter().stringToPictures(it.getAsString(COLUMN_PICTURES))!!
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
