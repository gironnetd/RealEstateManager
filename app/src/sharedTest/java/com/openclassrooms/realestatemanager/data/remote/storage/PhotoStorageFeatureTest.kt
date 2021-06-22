package com.openclassrooms.realestatemanager.data.remote.storage

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.Constants.SLASH
import com.openclassrooms.realestatemanager.util.Constants.THUMBNAIL_FILE_NAME
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_EMULATOR_HOST
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_STORAGE_DEFAULT_BUCKET
import com.openclassrooms.realestatemanager.util.ConstantsTest.FIREBASE_STORAGE_PORT
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoStorageFeatureTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var resources: Resources
    private lateinit var assets: AssetManager

    private lateinit var storage: FirebaseStorage

    private lateinit var photoStorage: PhotoStorageFeature

    @Before
    public override fun setUp() {
        super.setUp()
        assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        storage = FirebaseStorage.getInstance(FIREBASE_STORAGE_DEFAULT_BUCKET)
        storage.useEmulator(FIREBASE_EMULATOR_HOST, FIREBASE_STORAGE_PORT)

        photoStorage = PhotoStorageFeature(storage = storage)

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        fakePhotos = fakePhotos.sortedBy { it.id }

        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = firstPropertyId
        }

        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = secondPropertyId
        }

        fakePhotos = fakePhotos.sortedBy { it.id }
        fakePhotos.forEach { photo -> photo.bitmap = bitmapFromAsset(photo.id) }
    }

    @After
    public override fun tearDown() {
        photoStorage.deleteAllPhotos().blockingAwait()
        super.tearDown()
    }

    @Test
    fun given_photo_storage_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoStorage.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_storage_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoStorage.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoStorage.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_photo_storage_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoStorage.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        val expectedPhoto = photoStorage.findPhotoById(fakePhotos[0].id).blockingGet()
        assertThat(BitmapUtil.sameAs(fakePhotos[0].bitmap!!, expectedPhoto))
    }

    @Test
    fun given_photo_storage_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoStorage.findAllPhotos().blockingGet()

        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index]))
        }
    }

    @Test
    fun given_photo_storage_when_find_all_photos_then_found_successfully() {
        // Given photos list and When photos list saved
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoStorage.findAllPhotos().blockingGet()

        // Then returned photos in database is equal to given photos list
        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index]))
        }
    }

    @Test
    fun given_photo_storage_when_find_photo_by_id_then_found_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Bitmap = photoStorage.findPhotoById(photo.id).blockingGet()
        assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhoto))
    }

    @Test
    fun given_photo_storage_when_find_photos_by_ids_then_found_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()
        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        val expectedPhotos: List<Bitmap> = photoStorage.findPhotosByIds(photoIds).blockingGet()

        assertThat(BitmapUtil.sameAs(fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap!!, expectedPhotos[0]))
        assertThat(BitmapUtil.sameAs(fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap!!, expectedPhotos[1]))
    }

    @Test
    fun given_photo_storage_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            bitmap = decodeResource(resources, R.drawable.default_image)
        }
        photoStorage.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = photoStorage.findPhotoById(initialPhoto.id).blockingGet()

        assertThat(BitmapUtil.sameAs(finalPhoto, updatedPhoto.bitmap!!))
    }

    @Test
    fun given_photo_storage_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        photoStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                bitmap = decodeResource(resources, R.drawable.default_image)
            }
        }
        photoStorage.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = photoStorage.findPhotosByIds(ids).blockingGet()

        finalPhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo, updatedPhotos[index].bitmap!!))
        }
    }

    @Test
    fun given_photo_storage_when_delete_photo_by_id_then_deleted_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        assertThat(photoStorage.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoStorage.deletePhotoById(photo.id).blockingAwait()
        assertThat(photoStorage.findAllPhotos().blockingGet().contains(photo.bitmap))
            .isFalse()
    }

    @Test
    fun given_photo_storage_when_delete_photos_by_ids_then_deleted_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        photoStorage.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = photoStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap))
            .isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap))
            .isFalse()
    }

    @Test
    fun given_photo_storage_when_delete_photos_then_deleted_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoStorage.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        photoStorage.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = photoStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_photo_storage_when_delete_all_photos_then_deleted_successfully() {
        photoStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoStorage.findAllPhotos().blockingGet().size
        ).isEqualTo(fakePhotos.size)
        photoStorage.deleteAllPhotos().blockingAwait()
        assertThat(photoStorage.findAllPhotos().blockingGet()).isEmpty()
    }

    private fun bitmapFromAsset(fileName: String): Bitmap {
        val inputStream = assets.open(fileName + SLASH + THUMBNAIL_FILE_NAME)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap
    }

    companion object {
        const val firstPropertyId: String = "2orYJD9m1aAPbTKcrkBj"
        const val secondPropertyId: String = "AMEs0idV3ur4eqK2vF3O"
    }
}