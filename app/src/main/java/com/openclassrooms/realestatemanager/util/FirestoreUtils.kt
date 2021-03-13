package com.openclassrooms.realestatemanager.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openclassrooms.realestatemanager.models.*
import com.openclassrooms.realestatemanager.util.Constants.PICTURES_COLLECTION
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.NameType.FIRST_NAME
import net.andreinc.mockneat.types.enums.NameType.LAST_NAME
import java.time.LocalDate
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@RequiresApi(Build.VERSION_CODES.O)
class FirestoreUtils {

    companion object {

        private const val TAG = "FirestoreUtils"
        fun populateFirestore() {

            val firestore = Firebase.firestore

            val mockNeat = MockNeat.threadLocal();

            var agents: List<Agent> = listOf(
                    Agent(firstName = mockNeat.names().type(FIRST_NAME).get(),
                            lastName = mockNeat.names().type(LAST_NAME).get(),
                            email = mockNeat.emails().domain("gmail.com").get(),
                            phoneNumber = "01.20.02.34.45"
                    ),
                    Agent(firstName = mockNeat.names().type(FIRST_NAME).get(),
                            lastName = mockNeat.names().type(LAST_NAME).get(),
                            email = mockNeat.emails().domain("gmail.com").get(),
                            phoneNumber = "06.40.50.34.21"
                    ),
                    Agent(firstName = mockNeat.names().type(FIRST_NAME).get(),
                            lastName = mockNeat.names().type(LAST_NAME).get(),
                            email = mockNeat.emails().domain("gmail.com").get(),
                            phoneNumber = "08.13.21.45.53"
                    )
            )

            agents.forEach { agent ->
                val agentRef = firestore.collection("agents").document()
                agent.id = agentRef.id
                agentRef.set(agent)
            }

            val startDate = LocalDate.of(2010, 1, 1) //start date
            val start = startDate.toEpochDay();

            val endDate = LocalDate.now()//end date
            val end = endDate.toEpochDay();

            var properties: List<Property> = listOf(
                    Property(type = PropertyType.FLAT,
                            price = (10000..20000).random(),
                            surface = (20..40).random(),
                            rooms = (2..5).random(),
                            bedRooms = (1..3).random(),
                            bathRooms = (1..3).random(),
                            description = "Located rue Hoche, in the heart of the historic and dynamic district of Notre Dame, 5 min walk from the Palace, 5 min walk from Versailles Rive-Droite train station and 10 min walk from Versailles Rive-Gauche train station.\n" +
                                    "\n" +
                                    "This charming little house is located in a beautiful 18th century building and is close to all the shops on Rue de la Paroisse. Very calm and bright, its 5 windows overlook a paved courtyard. It has just been completely renovated with quality materials (solid oak parquet, Ressource paints, new high-end frames).\n" +
                                    "\n" +
                                    "On the ground floor: the tiled entrance continues with a beautiful oak staircase giving access to the living rooms.\n" +
                                    "The living / dining room opens onto an open kitchen (new furniture and appliances: oven, gas hob, fridge freezer, extractor, microwave, Nespresso coffee machine). Crockery provided.\n" +
                                    "Living room: sofa and new furniture (coffee table, armchairs, dining room table, chairs) Samsung TV.\n" +
                                    "Single room with 140 bed, plenty of storage space and old marble fireplace.\n" +
                                    "Bathroom with wc and washing machine.\n" +
                                    "\n" +
                                    "Linens provided (duvet, ears, sheets, towels, tea towels ..).\n" +
                                    "\n" +
                                    "Individual gas boiler.\n" +
                                    "\n" +
                                    "The double glazed windows have all been changed.",
                            address = Address(street = "34, rue Hoche",
                                    city = "Le Chesnay-Rocquencourt",
                                    postalCode = "78150",
                                    country = "France",
                                    state = "Yvelines",
                                    latitude = 48.82981227721342,
                                    longitude = 2.127166588301304
                            ),
                            interestPoints = mutableListOf(
                                    InterestPoint.BUSES,
                                    InterestPoint.PARK,
                                    InterestPoint.SCHOOL
                            ),
                            status = PropertyStatus.IN_SALE,

                            agentId = agents.toList().shuffled()[0].id,

                            entryDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                            soldDate = null
                    ),
                    Property(type = PropertyType.HOUSE,
                            price = (10000..20000).random(),
                            surface = (60..80).random(),
                            rooms = (2..5).random(),
                            bedRooms = (1..3).random(),
                            bathRooms = (1..3).random(),
                            description = "Close to the Palace of Versailles, the Sun King's doctor's farm. Charm, completely renovated and equipped. Beams over 550 years old, 70 cm thick walls, 5.50 m high ceilings, facing south. Duplex 70 m2 furnished with taste, bathroom in aged marble, wc. 3 large Vélux style roof windows, very bright, triple exposure, 1 bedroom on the mezzanine, living room. Private garden with stream. Near equestrian center, Saint-Nom-la-Bretèche golf course.",
                            address = Address(street = "3 Square Fantin Latour",
                                    city = "Le Chesnay-Rocquencourt",
                                    postalCode = "78150",
                                    country = "France",
                                    state = "Yvelines",
                                    latitude = 48.82958536116524,
                                    longitude = 2.125609030745346
                            ),
                            interestPoints = mutableListOf(
                                    InterestPoint.BUSES,
                                    InterestPoint.SCHOOL
                            ),
                            status = PropertyStatus.IN_SALE,
                            agentId = agents.toList().shuffled()[0].id,
                            entryDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                            soldDate = null
                    ),
                    Property(type = PropertyType.DUPLEX,
                            price = (10000..20000).random(),
                            surface = (20..40).random(),
                            rooms = (2..5).random(),
                            bedRooms = (1..5).random(),
                            bathRooms = (1..3).random(),
                            description = "Clear view, bright, sunny, double exposure South street and North courtyard, calm.\n" +
                                    "Collective central heating, comfort, economical (Charges including heating, water) double glazing,\n" +
                                    "\n" +
                                    "Entrance with storage, separate fitted kitchen (with crockery, stove, fridge, freezer, microwave, washing machine)\n" +
                                    "bathroom with window, large Italian shower, storage, wall mounted clothes rack.\n" +
                                    "Separate toilet, with storage, and storage space with wardrobe and shelves.\n" +
                                    "Living room with convertible sofa and storage, and dining area\n" +
                                    "bedroom facing courtyard, with storage and office area.\n" +
                                    "\n" +
                                    "Small quiet joint ownership. 2nd floor of 3.\n" +
                                    "\n" +
                                    "preferably by sms to keep contact details",
                            address = Address(street = "3 Square Fantin Latour",
                                    city = "Le Chesnay-Rocquencourt",
                                    postalCode = "78150",
                                    country = "France",
                                    state = "Yvelines",
                                    latitude = 48.829394665153714,
                                    longitude = 2.12793718802304
                            ),
                            interestPoints = mutableListOf(
                                    InterestPoint.BUSES,
                                    InterestPoint.PARK,
                                    InterestPoint.SUBWAY
                            ),
                            status = PropertyStatus.FOR_RENT,
                            agentId = agents.toList().shuffled()[0].id,
                            entryDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                            soldDate = null
                    ),
                    Property(
                            type = PropertyType.FLAT,
                            price = (10000..20000).random(),
                            surface = (20..40).random(),
                            rooms = (2..5).random(),
                            bedRooms = (1..3).random(),
                            bathRooms = (1..3).random(),
                            description = "Notre-Dame Richaud district, 2 rooms furnished completely renovated, 27.78 m2 + 1.30 m2.\n" +
                                    "\n" +
                                    "Near the Hoche and La Bruyère high schools, universities, ISIPCA, the school of architecture, the administrative and judicial city, the market, the Castle and its park.\n" +
                                    "\n" +
                                    "Secure building, intercom, armored door with peephole. 2nd floor, no lift.\n" +
                                    "\n" +
                                    "Entrance, living room with view on the Richaud district, quiet bedroom overlooking the courtyard, fitted kitchen open to living room, bathroom with walk-in shower.\n" +
                                    "\n" +
                                    "Equipment: 4 * refrigerator, hotplates, extractor hood, oven and microwave, vacuum cleaner, Queen size bed, sofa bed for third bed.\n" +
                                    "\n" +
                                    "Additional equipment: dishwasher, washing machine, TV, numerous kitchen utensils and small appliances (Dolce Gusto coffee maker, kettle, toaster, iron, etc.), large reception crockery. Linens provided.\n" +
                                    "\n" +
                                    "Possibility to park a bicycle. Well connected :\n" +
                                    "- 4 minutes walk from Versailles Rive Droite station (direct line La Défense, St Lazare)\n" +
                                    "- 10 minutes walk from Versailles Rive Gauche station (direct Saint-Michel)\n" +
                                    "- Bus stop at the corner of the street\n" +
                                    "\n" +
                                    "A small storage room on the middle floor for various storage: household accessories, suitcases, tools ...\n" +
                                    "\n" +
                                    "Individual hot water and electric heating.",
                            address = Address(street = "2-8 Square de Castiglione",
                                    city = "Le Chesnay-Rocquencourt",
                                    postalCode = "78150",
                                    country = "France",
                                    state = "Yvelines",
                                    latitude = 48.829394665153714,
                                    longitude = 2.1244825030303334
                            ),
                            interestPoints = mutableListOf(
                                    InterestPoint.BUSES,
                                    InterestPoint.PARK,
                                    InterestPoint.SHOP
                            ),
                            status = PropertyStatus.SOLD,
                            agentId = agents.toList().shuffled()[0].id,
                            entryDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                            soldDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                    ),
                    Property(price = (10000..20000).random(),
                            surface = (20..40).random(),
                            rooms = (2..5).random(),
                            bedRooms = (1..3).random(),
                            bathRooms = (1..3).random(),
                            description = "Located rue Hoche, in the heart of the historic and dynamic district of Notre Dame, 5 min walk from the Palace, 5 min walk from Versailles Rive-Droite train station and 10 min walk from Versailles Rive-Gauche train station.\n" +
                                    "\n" +
                                    "This charming little house is located in a beautiful 18th century building and is close to all the shops on Rue de la Paroisse. Very calm and bright, its 5 windows overlook a paved courtyard. It has just been completely renovated with quality materials (solid oak parquet, Ressource paints, new high-end frames).\n" +
                                    "\n" +
                                    "On the ground floor: the tiled entrance continues with a beautiful oak staircase giving access to the living rooms.\n" +
                                    "The living / dining room opens onto an open kitchen (new furniture and appliances: oven, gas hob, fridge freezer, extractor, microwave, Nespresso coffee machine). Crockery provided.\n" +
                                    "Living room: sofa and new furniture (coffee table, armchairs, dining room table, chairs) Samsung TV.\n" +
                                    "Single room with 140 bed, plenty of storage space and old marble fireplace.\n" +
                                    "Bathroom with wc and washing machine.\n" +
                                    "\n" +
                                    "Linens provided (duvet, ears, sheets, towels, tea towels ..).\n" +
                                    "\n" +
                                    "Individual gas boiler.\n" +
                                    "\n" +
                                    "The double glazed windows have all been changed.\n" +
                                    "\n" +
                                    "Bicycle garage in the yard.\n" +
                                    "Available now.",
                            address = Address(street = "5 Rue la Pérouse",
                                    city = "Le Chesnay-Rocquencourt",
                                    postalCode = "78150",
                                    country = "France",
                                    state = "Yvelines",
                                    latitude = 48.829275,
                                    longitude = 2.123710
                            ),
                            interestPoints = mutableListOf(
                                    InterestPoint.BUSES,
                                    InterestPoint.PARK,
                                    InterestPoint.SHOP,
                                    InterestPoint.SUBWAY
                            ),
                            status = PropertyStatus.IN_SALE,
                            agentId = agents.toList().shuffled()[0].id,
                            entryDate = Date(ThreadLocalRandom.current()
                                    .nextLong(start, end)),
                            soldDate = null
                    )
            )

            for (property in properties) {
                val documentRef = firestore.collection(PROPERTIES_COLLECTION).document()
                property.id = documentRef.id

                var pictures: List<Picture> = listOf(
                        Picture(
                                description = "",
                                type = PictureType.MAIN,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.LOUNGE,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.LOUNGE,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.BATHROOM,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.BEDROOM,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.BEDROOM,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.BEDROOM,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.KITCHEN,
                        ),
                        Picture(
                                description = "",
                                type = PictureType.FACADE,
                        ),
                )

                for (picture in pictures) {
                    val pictureRef = firestore.collection(PROPERTIES_COLLECTION)
                            .document(property.id)
                            .collection(PICTURES_COLLECTION)
                            .document()
                    picture.id = pictureRef.id

                    if (picture.type != PictureType.MAIN) {
                        pictureRef.set(picture)
                    } else {
                        property.mainPicture = picture
                    }
                }
                documentRef.set(property)
            }
        }
    }
}