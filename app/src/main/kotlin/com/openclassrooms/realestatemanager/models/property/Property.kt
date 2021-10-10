package com.openclassrooms.realestatemanager.models.property

import android.content.ContentValues
import android.content.res.Resources
import android.database.Cursor
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.NonNull
import androidx.room.*
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_CITY
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_COUNTRY
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_LATITUDE
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_LONGITUDE
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_POSTAL_CODE
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_STATE
import com.openclassrooms.realestatemanager.models.property.Address.Companion.COLUMN_ADDRESS_STREET
import com.openclassrooms.realestatemanager.models.property.Property.Companion.TABLE_NAME
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = TABLE_NAME)
data class Property (

    @PrimaryKey
        @ColumnInfo(index = true, name = COLUMN_ID)
        @SerializedName(value = "id")
        var id: String = Firebase.firestore.collection(PROPERTIES_COLLECTION).document().id,

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
        var address: Address = Address(),

    @ColumnInfo(name = "interest_points")
        var interestPoints: MutableList<InterestPoint> = mutableListOf(InterestPoint.NONE),

    var status: PropertyStatus = PropertyStatus.NONE,

    @ColumnInfo(name = "agent_id")
        var agentId: String? = null,

    @ColumnInfo(name = "main_photo_id")
        var mainPhotoId: String? = null,

    @ColumnInfo(name = "entry_date")
        var entryDate: Date? = null,

    @ColumnInfo(name = "sold_date")
        var soldDate: Date? = null,

    @Ignore
        @get:Exclude
        var photos: MutableList<Photo> = mutableListOf(),

    @ColumnInfo(name = "locally_updated")
        @get:Exclude
        var locallyUpdated: Boolean = false,

    @ColumnInfo(name = "locally_created")
        @get:Exclude
        var locallyCreated: Boolean = false
) : Parcelable {

        fun deepCopy(id: String = this.id, type: PropertyType = this.type, price: Int = this.price,
                     surface: Int = this.surface, rooms: Int = this.rooms, bedRooms: Int = this.bedRooms,
                     bathRooms: Int = this.bathRooms, description: String = this.description,
                     address: Address = this.address.deepCopy(),
                     interestPoints: MutableList<InterestPoint> = this.interestPoints,
                     status: PropertyStatus = this.status, agentId: String? = this.agentId,
                     mainPhotoId: String? = this.mainPhotoId, entryDate: Date? = this.entryDate,
                     soldDate: Date? = this.soldDate,
                     photos: MutableList<Photo> = this.photos.map { photo -> photo.deepCopy() }.toMutableList(),
                     locallyUpdated: Boolean = this.locallyUpdated,
                     locallyCreated: Boolean = this.locallyCreated
        ) = Property(id, type, price, surface, rooms, bedRooms, bathRooms, description, address,
                interestPoints, status, agentId, mainPhotoId, entryDate, soldDate,
                photos, locallyUpdated, locallyCreated)

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
                mainPhotoId = cursor.getString(cursor.getColumnIndex(COLUMN_MAIN_PHOTO_ID))
                if(!cursor.isNull(cursor.getColumnIndex(COLUMN_ENTRY_DATE))) {
                        entryDate = DateConverter()
                                .fromTimestamp(cursor.getLong(cursor.getColumnIndex(
                                    COLUMN_ENTRY_DATE
                                )))!!
                }
                if(!cursor.isNull(cursor.getColumnIndex(COLUMN_SOLD_DATE))) {
                        soldDate = DateConverter()
                                .fromTimestamp(cursor.getLong(cursor.getColumnIndex(COLUMN_SOLD_DATE)))!!
                }

                locallyUpdated = cursor.getInt(cursor.getColumnIndex(COLUMN_LOCALLY_UPDATED)) > 0
                locallyCreated = cursor.getInt(cursor.getColumnIndex(COLUMN_LOCALLY_CREATED)) > 0
        }

        constructor( property: Property) : this() {
                id = property.id
                type = property.type
                price = property.price
                surface = property.surface
                rooms = property.rooms
                bedRooms = property.bedRooms
                bathRooms = property.bathRooms
                description = property.description
                address = property.address

                property.interestPoints.forEach { interestPoint ->
                        interestPoints.add(interestPoint)
                }

                status = property.status
                agentId = property.agentId
                mainPhotoId = property.mainPhotoId
                entryDate = property.entryDate
                soldDate = property.soldDate
                photos = property.photos
                locallyUpdated = property.locallyUpdated
                locallyCreated = property.locallyCreated
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

                /** The name of the interest points column.  */
                const val COLUMN_INTEREST_POINTS = "interest_points"

                /** The name of the property status column.  */
                const val COLUMN_PROPERTY_STATUS = "status"

                /** The name of the agent id column.  */
                const val COLUMN_AGENT_ID = "agent_id"

                /** The name of the main Photo column.  */
                const val COLUMN_MAIN_PHOTO_ID = "main_photo_id"

                /** The name of the entry date column.  */
                const val COLUMN_ENTRY_DATE = "entry_date"

                /** The name of the sold date column.  */
                const val COLUMN_SOLD_DATE = "sold_date"

                /** The name of the locally updated column.  */
                const val COLUMN_LOCALLY_UPDATED = "locally_updated"

                /** The name of the locally created column.  */
                const val COLUMN_LOCALLY_CREATED = "locally_created"

                @NonNull
                fun fromContentValues(values: ContentValues?): Property {
                        val property = Property()
                        values?.let {
                                if ( it.containsKey(COLUMN_ID)) {
                                        property.id = it.getAsString(COLUMN_ID)
                                }
                                if (it.containsKey(COLUMN_PROPERTY_TYPE)) {
                                        property.type = PropertyType.valueOf(it.getAsString(
                                            COLUMN_PROPERTY_TYPE
                                        ))
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

                                if (it.containsKey(COLUMN_ADDRESS_STREET)) {
                                         property.address.street = it.getAsString(COLUMN_ADDRESS_STREET)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_CITY)) {
                                        property.address.city = it.getAsString(COLUMN_ADDRESS_CITY)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_POSTAL_CODE)) {
                                        property.address.postalCode = it.getAsString(COLUMN_ADDRESS_POSTAL_CODE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_COUNTRY)) {
                                        property.address.country = it.getAsString(COLUMN_ADDRESS_COUNTRY)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_STATE)) {
                                        property.address.state = it.getAsString(COLUMN_ADDRESS_STATE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_LATITUDE)) {
                                        property.address.latitude = it.getAsDouble(COLUMN_ADDRESS_LATITUDE)
                                }

                                if (it.containsKey(COLUMN_ADDRESS_LONGITUDE)) {
                                        property.address.longitude = it.getAsDouble(COLUMN_ADDRESS_LONGITUDE)
                                }

                                if (it.containsKey(COLUMN_INTEREST_POINTS)) {
                                        property.interestPoints = InterestPointConverter().stringToInterestPoints(it.getAsString(
                                            COLUMN_INTEREST_POINTS
                                        ))!!
                                }

                                if (it.containsKey(COLUMN_PROPERTY_STATUS)) {
                                        property.status = PropertyStatus.valueOf(it.getAsString(
                                            COLUMN_PROPERTY_STATUS
                                        ))
                                }

                                if (it.containsKey(COLUMN_AGENT_ID)) {
                                        property.agentId = it.getAsString(COLUMN_AGENT_ID)
                                }

                                if (it.containsKey(COLUMN_MAIN_PHOTO_ID)) {
                                        property.mainPhotoId = it.getAsString(COLUMN_MAIN_PHOTO_ID)
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

                                if(it.containsKey(COLUMN_LOCALLY_UPDATED)) {
                                        property.locallyUpdated = it.getAsBoolean(
                                            COLUMN_LOCALLY_UPDATED
                                        )
                                }

                                if(it.containsKey(COLUMN_LOCALLY_CREATED)) {
                                        property.locallyCreated = it.getAsBoolean(
                                            COLUMN_LOCALLY_CREATED
                                        )
                                }
                        }
                        return property
                }
        }

        fun titleInToolbar(resources: Resources): String {
                return if((address.street + ", " +
                        address.postalCode + " " + address.city).trim() != ",") {
                        address.street + ", " +
                                address.postalCode + " " + address.city
                } else {
                        resources.getString(R.string.no_address_found)
                }
        }

        fun addressInList(resources: Resources): String {
                return if((address.street + ",\n" + address.postalCode + " " + address.city).trim() != ",") {
                        address.street + ",\n" + address.postalCode + " " + address.city
                } else {
                        resources.getString(R.string.no_address_found)
                }
        }

        fun copy(): Property {
                val property: String = Gson().toJson(this, Property::class.java)
                return Gson().fromJson(property, Property::class.java)
        }

        fun update(other: Property) {
                if (id != other.id) { id = other.id }
                if (type != other.type) { type = other.type }
                if (price != other.price) { price = other.price }
                if (surface != other.surface) { surface = other.surface }
                if (rooms != other.rooms) { rooms = other.rooms }
                if (bedRooms != other.bedRooms) { bedRooms = other.bedRooms }
                if (bathRooms != other.bathRooms) { bathRooms = other.bathRooms }
                if (description != other.description){ description = other.description }
                if (address != other.address) { address = other.address }
                if (interestPoints != other.interestPoints) { interestPoints = other.interestPoints }
                if (status != other.status) { status = other.status }
                if (agentId != other.agentId) { agentId = other.agentId }
                if (mainPhotoId != other.mainPhotoId) { mainPhotoId = other.mainPhotoId }
                if (entryDate != other.entryDate) { entryDate = other.entryDate }
                if (soldDate != other.soldDate) { soldDate = other.soldDate }
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Property

                // if (id != other.id) return false
                if (type != other.type) return false
                if (price != other.price) return false
                if (surface != other.surface) return false
                if (rooms != other.rooms) return false
                if (bedRooms != other.bedRooms) return false
                if (bathRooms != other.bathRooms) return false
                if (description != other.description) return false
                if (address != other.address) return false
                if(interestPoints.size != 1 && interestPoints.contains(InterestPoint.NONE)) {
                        interestPoints.remove(InterestPoint.NONE)
                }
                if (interestPoints != other.interestPoints) return false
                if (status != other.status) return false
                if (agentId != other.agentId) return false
                if (mainPhotoId != other.mainPhotoId) return false
                if (entryDate != other.entryDate) return false
                if (soldDate != other.soldDate) return false
                // if (photos != other.photos) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + type.hashCode()
                result = 31 * result + price
                result = 31 * result + surface
                result = 31 * result + rooms
                result = 31 * result + bedRooms
                result = 31 * result + bathRooms
                result = 31 * result + description.hashCode()
                result = 31 * result + (address.hashCode())
                result = 31 * result + interestPoints.hashCode()
                result = 31 * result + status.hashCode()
                result = 31 * result + (agentId?.hashCode() ?: 0)
                result = 31 * result + (mainPhotoId?.hashCode() ?: 0)
                result = 31 * result + entryDate.hashCode()
                result = 31 * result + (soldDate?.hashCode() ?: 0)
                return result
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
