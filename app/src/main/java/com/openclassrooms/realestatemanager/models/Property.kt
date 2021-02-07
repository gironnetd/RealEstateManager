package com.openclassrooms.realestatemanager.models

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "properties")
data class Property(
        @PrimaryKey
        @ColumnInfo(name = "property_id")
        @SerializedName(value = "id")
        var propertyId: String = "",

        @SerializedName(value = "type")
        @ColumnInfo(name = "type")
        var propertyType: PropertyType = PropertyType.NONE,

        @ColumnInfo(name = "price")
        val price: Int = 0,

        @ColumnInfo(name = "surface")
        val surface: Int = 0,

        @ColumnInfo(name = "rooms")
        val rooms: Int = 0,

        @ColumnInfo(name = "bedrooms")
        val bedRooms: Int = 0,

        @ColumnInfo(name = "bathrooms")
        val bathRooms: Int = 0,

        @ColumnInfo(name = "description")
        val description: String = "",

        @Embedded
        val address: Address? = null,

        @ColumnInfo(name = "interest_points")
        var interestPoints: MutableList<InterestPoint> = mutableListOf(),
        var status: PropertyStatus = PropertyStatus.NONE,

        @ColumnInfo(name = "agent_id")
        var agentId: String? = null,

        @Embedded(prefix = "main_picture_")
        var mainPicture: Picture? = null,

        @ColumnInfo(name = "entry_date")
        var entryDate: Date = Date(),

        @ColumnInfo(name = "sold_date")
        var soldDate: Date? = null,
)

class DateConverter {
        @TypeConverter
        fun fromTimestamp(value: Long?): Date? {
                return value?.let { Date(it) }
        }

        @TypeConverter
        fun dateToTimestamp(date: Date?): Long? {
                return date?.time?.toLong()
        }
}
