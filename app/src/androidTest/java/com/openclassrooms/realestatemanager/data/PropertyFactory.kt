package com.openclassrooms.realestatemanager.data

import android.graphics.BitmapFactory.decodeResource
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Property
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.StringType

class PropertyFactory {

    companion object Factory {

        fun createProperty(fakeProperties: List<Property>): Property {
            val property = fakeProperties.random()
            property.updated = true
            val mockNeat = MockNeat.threadLocal()

            property.description = mockNeat.strings().size(40).type(StringType.LETTERS).get()
            property.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

            property.address!!.street = mockNeat.strings().size(12).type(StringType.LETTERS).get()
            property.address!!.city = mockNeat.cities().capitalsEurope().get()
            property.address!!.postalCode = mockNeat.strings().size(5).type(StringType.NUMBERS).get()
            property.address!!.country = mockNeat.countries().names().get()
            property.address!!.state = mockNeat.usStates().get()

            property.bathRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
            property.bedRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
            property.rooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()

            property.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

            val updatedPhotoId = property.photos.random().id

            with(property.photos.single { photo -> photo.id == updatedPhotoId }) {
                bitmap = decodeResource(InstrumentationRegistry.getInstrumentation().targetContext.resources, R.drawable.default_image)
                updated = true
            }

            return property
        }
    }
}