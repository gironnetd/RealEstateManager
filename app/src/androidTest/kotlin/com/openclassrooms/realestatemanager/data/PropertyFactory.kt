package com.openclassrooms.realestatemanager.data

import android.graphics.BitmapFactory.decodeResource
import androidx.test.platform.app.InstrumentationRegistry
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.property.Property
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.StringType

class PropertyFactory {

    companion object Factory {

        fun createProperty(fakeProperty: Property, updates: Boolean = false, creations: Boolean = false): Property {

            val mockNeat = MockNeat.threadLocal()

            fakeProperty.description = mockNeat.strings().size(40).type(StringType.LETTERS).get()
            fakeProperty.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

            fakeProperty.address!!.street = mockNeat.strings().size(12).type(StringType.LETTERS).get()
            fakeProperty.address!!.city = mockNeat.cities().capitalsEurope().get()
            fakeProperty.address!!.postalCode = mockNeat.strings().size(5).type(StringType.NUMBERS).get()
            fakeProperty.address!!.country = mockNeat.countries().names().get()
            fakeProperty.address!!.state = mockNeat.usStates().get()

            fakeProperty.bathRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
            fakeProperty.bedRooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()
            fakeProperty.rooms = mockNeat.strings().size(1).type(StringType.NUMBERS).get().toInt()

            fakeProperty.surface = mockNeat.strings().size(3).type(StringType.NUMBERS).get().toInt()

            fakeProperty.locallyUpdated  = updates
            fakeProperty.locallyCreated = creations

            if(updates) {
                val updatedPhotoId = fakeProperty.photos.random().id
                with(fakeProperty.photos.single { photo -> photo.id == updatedPhotoId }) {
                    bitmap = decodeResource(InstrumentationRegistry.getInstrumentation().targetContext.resources, R.drawable.default_image)
                    locallyUpdated = updates
                }
            }

            if(creations) {
                fakeProperty.photos.onEach { photo -> photo.locallyCreated = true }
            }

            return fakeProperty
        }
    }
}