package com.openclassrooms.realestatemanager.data.remote.data

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
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_EMULATOR_HOST
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_FIRESTORE_PORT
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoRemoteDataSourceTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var firstPropertyId: String
    private lateinit var secondPropertyId: String
    private lateinit var firestore : FirebaseFirestore

    private lateinit var photoRemoteDataSource: PhotoRemoteDataSource

    @Before
    public override fun setUp() {
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_FIRESTORE_PORT)
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        firestore.firestoreSettings = settings

        photoRemoteDataSource = PhotoRemoteDataSource(firestore = firestore)

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
        photoRemoteDataSource.deleteAllPhotos().blockingAwait()
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
    fun given_photo_remote_data_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoRemoteDataSource.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_remote_data_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoRemoteDataSource.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoRemoteDataSource.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_photo_remote_data_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoRemoteDataSource.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoRemoteDataSource.findPhotoById(fakePhotos[0].id).blockingGet()).isEqualTo(fakePhotos[0])
    }

    @Test
    fun given_photo_remote_data_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        fakePhotos = fakePhotos.sortedBy { it.id }
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet()).isEqualTo(fakePhotos)
    }

    @Test
    fun given_photo_remote_data_source_when_find_all_photos_then_found_successfully() {
        // Given photos list
        // When photos list saved
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet()).isEqualTo(fakePhotos)
    }

    @Test
    fun given_photo_remote_data_source_when_find_photo_by_id_then_found_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = photoRemoteDataSource.findPhotoById(photo.id).blockingGet()
        assertThat(expectedPhoto).isEqualTo(photo)
    }

    @Test
    fun given_photo_remote_data_source_when_find_photos_by_ids_then_found_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()
        var photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        var expectedPhotos: List<Photo> = photoRemoteDataSource.findPhotosByIds(photoIds).blockingGet()

        assertThat(expectedPhotos[0]).isEqualTo(fakePhotos.single { photo -> photo.id == photoIds[0] })
        assertThat(expectedPhotos[1]).isEqualTo(fakePhotos.single { photo -> photo.id == photoIds[1] })
    }

    @Test
    fun given_photo_remote_data_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = PhotoType.values().first { type -> type != initialPhoto.type }
        }
        photoRemoteDataSource.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = photoRemoteDataSource.findPhotoById(initialPhoto.id).blockingGet()
        assertThat(finalPhoto).isEqualTo(updatedPhoto)
    }

    @Test
    fun given_photo_remote_data_source_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = PhotoType.values().first { type -> type != initialPhotos[index].type }
            }
        }
        photoRemoteDataSource.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = photoRemoteDataSource.findAllPhotos().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }

        assertThat(finalPhotos).isEqualTo(updatedPhotos.toList())
    }

    @Test
    fun given_photo_remote_data_source_when_delete_photo_by_id_then_deleted_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoRemoteDataSource.deletePhotoById(photo.id).blockingAwait()
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet().contains(photo))
            .isFalse()
    }

    @Test
    fun given_photo_remote_data_source_when_delete_photos_by_ids_then_deleted_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        photoRemoteDataSource.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = photoRemoteDataSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] }))
            .isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] }))
            .isFalse()
    }

    @Test
    fun given_photo_remote_data_source_when_delete_photos_then_deleted_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        photoRemoteDataSource.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = photoRemoteDataSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_photo_remote_data_source_when_delete_all_photos_then_deleted_successfully() {
        photoRemoteDataSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet().size
        ).isEqualTo(fakePhotos.size)
        photoRemoteDataSource.deleteAllPhotos().blockingAwait()
        assertThat(photoRemoteDataSource.findAllPhotos().blockingGet()).isEmpty()
    }
}