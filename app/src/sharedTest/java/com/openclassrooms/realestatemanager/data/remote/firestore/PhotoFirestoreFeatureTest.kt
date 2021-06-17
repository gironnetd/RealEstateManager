package com.openclassrooms.realestatemanager.data.remote.firestore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoFirestoreFeatureTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var firstPropertyId: String
    private lateinit var secondPropertyId: String
    private lateinit var firestore : FirebaseFirestore

    private lateinit var photoFirestore: PhotoFirestoreFeature

    @Before
    public override fun setUp() {
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()

        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = settings

        photoFirestore = PhotoFirestoreFeature(firestore = firestore)

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        fakePhotos = fakePhotos.sortedBy { it.id }

        firstPropertyId = firestore.collection(Constants.PROPERTIES_COLLECTION).document().id

        firestore.collection(Constants.PROPERTIES_COLLECTION).document(firstPropertyId)
            .set(hashMapOf(Property.COLUMN_PROPERTY_ID to firstPropertyId))

        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = firstPropertyId
        }

        secondPropertyId = firestore.collection(Constants.PROPERTIES_COLLECTION).document().id

        firestore.collection(Constants.PROPERTIES_COLLECTION).document(secondPropertyId)
            .set(hashMapOf(Property.COLUMN_PROPERTY_ID to secondPropertyId))

        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = secondPropertyId
        }

        fakePhotos = fakePhotos.sortedBy { it.id }

        super.setUp()
    }

    @After
    public override fun tearDown() {
        photoFirestore.deleteAllPhotos().blockingAwait()
        Completable.create { emitter ->
            firestore.collection(Constants.PROPERTIES_COLLECTION).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { result ->
                        result.documents.forEach { document ->
                            document.reference.delete()

                            if (document.id == result.documents.last().id) {
                                emitter.onComplete()
                            }
                        }
                    }
                }
            }
        }.blockingAwait()

        firestore.terminate()
        super.tearDown()
    }

    @Test
    fun given_photo_firestore_when_save_photos_then_counted_successfully() {

        // Given photos list and When photos list saved
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoFirestore.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_firestore_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoFirestore.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoFirestore.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_photo_firestore_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoFirestore.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoFirestore.findPhotoById(fakePhotos[0].id).blockingGet()).isEqualTo(fakePhotos[0])
    }

    @Test
    fun given_photo_firestore_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        fakePhotos = fakePhotos.sortedBy { it.id }
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        assertThat(photoFirestore.findAllPhotos().blockingGet()).isEqualTo(fakePhotos)
    }

    @Test
    fun given_photo_firestore_when_find_all_photos_then_found_successfully() {
        // Given photos list
        // When photos list saved
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        assertThat(photoFirestore.findAllPhotos().blockingGet()).isEqualTo(fakePhotos)
    }

    @Test
    fun given_photo_firestore_when_find_photo_by_id_then_found_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = photoFirestore.findPhotoById(photo.id).blockingGet()
        assertThat(expectedPhoto).isEqualTo(photo)
    }

    @Test
    fun given_photo_firestore_when_find_photos_by_ids_then_found_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()
        var photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        var expectedPhotos: List<Photo> = photoFirestore.findPhotosByIds(photoIds).blockingGet()

        assertThat(expectedPhotos[0]).isEqualTo(fakePhotos.single { photo -> photo.id == photoIds[0] })
        assertThat(expectedPhotos[1]).isEqualTo(fakePhotos.single { photo -> photo.id == photoIds[1] })
    }

    @Test
    fun given_photo_firestore_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = PhotoType.values().first { type -> type != initialPhoto.type }
        }
        photoFirestore.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = photoFirestore.findPhotoById(initialPhoto.id).blockingGet()
        assertThat(finalPhoto).isEqualTo(updatedPhoto)
    }

    @Test
    fun given_photo_firestore_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = PhotoType.values().first { type -> type != initialPhotos[index].type }
            }
        }
        photoFirestore.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = photoFirestore.findAllPhotos().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }

        assertThat(finalPhotos).isEqualTo(updatedPhotos.toList())
    }

    @Test
    fun given_photo_firestore_when_delete_photo_by_id_then_deleted_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        assertThat(photoFirestore.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoFirestore.deletePhotoById(photo.id).blockingAwait()
        assertThat(photoFirestore.findAllPhotos().blockingGet().contains(photo))
            .isFalse()
    }

    @Test
    fun given_photo_firestore_when_delete_photos_by_ids_then_deleted_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        photoFirestore.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = photoFirestore.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] }))
            .isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] }))
            .isFalse()
    }

    @Test
    fun given_photo_firestore_when_delete_photos_then_deleted_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoFirestore.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        photoFirestore.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = photoFirestore.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_photo_firestore_when_delete_all_photos_then_deleted_successfully() {
        photoFirestore.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoFirestore.findAllPhotos().blockingGet().size
        ).isEqualTo(fakePhotos.size)
        photoFirestore.deleteAllPhotos().blockingAwait()
        assertThat(photoFirestore.findAllPhotos().blockingGet()).isEmpty()
    }
}