package com.openclassrooms.realestatemanager.data.remote.source

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.remote.data.PhotoRemoteDataSource
import com.openclassrooms.realestatemanager.data.remote.storage.PhotoRemoteStorageSource
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.models.Property.Companion.COLUMN_PROPERTY_ID
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.Constants.PROPERTIES_COLLECTION
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_EMULATOR_HOST
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_FIRESTORE_PORT
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_STORAGE_DEFAULT_BUCKET
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_STORAGE_PORT
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoRemoteSourceTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var firstPropertyId: String
    private lateinit var secondPropertyId: String

    lateinit var firestore : FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var resources: Resources

    private lateinit var photoRemoteData: PhotoRemoteDataSource
    private lateinit var photoRemoteStorage: PhotoRemoteStorageSource
    private lateinit var remoteSource: PhotoRemoteSource

    @Before
    public override fun setUp() {
        super.setUp()
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_FIRESTORE_PORT)
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        firestore.firestoreSettings = settings

        photoRemoteData = PhotoRemoteDataSource(firestore)

        storage = FirebaseStorage.getInstance(FIREBASE_STORAGE_DEFAULT_BUCKET)
        storage.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_STORAGE_PORT)

        photoRemoteStorage = PhotoRemoteStorageSource(storage = storage)

        remoteSource = PhotoRemoteSource(remoteData = photoRemoteData, remoteStorage = photoRemoteStorage)

        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        fakePhotos = fakePhotos.sortedBy { it.id }

        firstPropertyId = firestore.collection(PROPERTIES_COLLECTION).document().id

        firestore.collection(PROPERTIES_COLLECTION).document(firstPropertyId)
            .set(hashMapOf(COLUMN_PROPERTY_ID to firstPropertyId))

        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = firstPropertyId
        }

        secondPropertyId = firestore.collection(PROPERTIES_COLLECTION).document().id

        firestore.collection(PROPERTIES_COLLECTION).document(secondPropertyId)
            .set(hashMapOf(COLUMN_PROPERTY_ID to secondPropertyId))

        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = secondPropertyId
        }

        fakePhotos = fakePhotos.sortedBy { it.id }
        fakePhotos.forEach { photo -> photo.bitmap = BitmapUtil.bitmapFromAsset(
            InstrumentationRegistry.getInstrumentation().targetContext,
            photo.id)
        }
    }

    @After
    public override fun tearDown() {
        if(remoteSource.count().blockingGet() != 0) {
            remoteSource.deleteAllPhotos().blockingAwait()
        }

        Completable.create { emitter ->
            firestore.collection(PROPERTIES_COLLECTION).get().addOnCompleteListener { task ->
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
    fun given_remote_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(remoteSource.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_remote_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(remoteSource.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(remoteSource.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_remote_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        remoteSource.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertSameAs(actual = fakePhotos[0],expected = remoteSource.findPhotoById(fakePhotos[0].id).blockingGet())
    }

    @Test
    fun given_remote_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        fakePhotos = fakePhotos.sortedBy { it.id }
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        val expectedPhotos = remoteSource.findAllPhotos().blockingGet().sortedBy { it.id }
        fakePhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = expectedPhotos[index])
        }
    }

    @Test
    fun given_remote_source_when_find_all_photos_then_found_successfully() {
        // Given photos list and When photos list saved
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        val expectedPhotos = remoteSource.findAllPhotos().blockingGet().sortedBy { it.id }

        fakePhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = expectedPhotos[index])
        }
    }

    @Test
    fun given_remote_source_when_find_photo_by_id_then_found_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = remoteSource.findPhotoById(photo.id).blockingGet()
        assertSameAs(actual = photo,expected = expectedPhoto)
    }

    @Test
    fun given_remote_source_when_find_photos_by_ids_then_found_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()
        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        var expectedPhotos: List<Photo> = remoteSource.findPhotosByIds(photoIds).blockingGet()

        expectedPhotos = expectedPhotos.sortedBy { it.id }
        assertSameAs(actual = fakePhotos.single { photo -> photo.id == photoIds[0] },expected = expectedPhotos[0])
        assertSameAs(actual = fakePhotos.single { photo -> photo.id == photoIds[1] },expected = expectedPhotos[1])
    }

    @Test
    fun given_remote_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        remoteSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = PhotoType.values().first { type -> type != initialPhoto.type }
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
        }
        remoteSource.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = remoteSource.findPhotoById(initialPhoto.id).blockingGet()
        assertSameAs(actual = updatedPhoto,expected = finalPhoto)
    }

    @Test
    fun given_remote_source_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        remoteSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = PhotoType.values().first { type -> type != initialPhotos[index].type }
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
            }
        }
        remoteSource.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = remoteSource.findAllPhotos().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }

        updatedPhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = finalPhotos[index])
        }
    }

    @Test
    fun given_remote_source_when_delete_photo_by_id_then_deleted_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        assertThat(remoteSource.count().blockingGet()).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        remoteSource.deletePhotoById(photo.id).blockingAwait()
        assertThat(remoteSource.findAllPhotos().blockingGet().contains(photo))
            .isFalse()
    }

    @Test
    fun given_remote_source_when_delete_photos_by_ids_then_deleted_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        remoteSource.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = remoteSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] })).isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] })).isFalse()
    }

    @Test
    fun given_remote_source_when_delete_photos_then_deleted_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(remoteSource.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        remoteSource.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = remoteSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_remote_source_when_delete_all_photos_then_deleted_successfully() {
        remoteSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(remoteSource.count().blockingGet()).isEqualTo(fakePhotos.size)
        remoteSource.deleteAllPhotos().blockingAwait()
        assertThat(remoteSource.findAllPhotos().blockingGet()).isEmpty()
    }

    private fun assertSameAs(actual: Photo, expected: Photo) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.propertyId).isEqualTo(expected.propertyId)
        assertThat(actual.mainPhoto).isEqualTo(expected.mainPhoto)
        assertThat(actual.description).isEqualTo(expected.description)
        assertThat(actual.type).isEqualTo(expected.type)
        assertThat(BitmapUtil.sameAs(actual.bitmap!!, expected.bitmap!!))
    }
}