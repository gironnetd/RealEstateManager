package com.openclassrooms.realestatemanager.data.local.storage

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoLocalStorageSourceTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var resources: Resources
    private lateinit var assets: AssetManager

    private lateinit var photoLocalStorage: PhotoLocalStorageSource

    @Before
    public override fun setUp() {
        super.setUp()
        assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        photoLocalStorage = PhotoLocalStorageSource(cacheDir =
        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)

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
        if(photoLocalStorage.count().blockingGet() != 0) {
            photoLocalStorage.deleteAllPhotos().blockingAwait()
        }
        super.tearDown()
    }

    @Test
    fun given_photo_local_storage_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoLocalStorage.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_local_storage_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoLocalStorage.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoLocalStorage.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_photo_local_storage_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoLocalStorage.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        val expectedPhoto = photoLocalStorage.findPhotoById(fakePhotos[0].id).blockingGet()
        assertThat(BitmapUtil.sameAs(fakePhotos[0].bitmap!!, expectedPhoto))
    }

    @Test
    fun given_photo_local_storage_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoLocalStorage.findAllPhotos().blockingGet()

        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index]))
        }
    }

    @Test
    fun given_photo_local_storage_source_when_find_all_photos_then_found_successfully() {
        // Given photos list and When photos list saved
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoLocalStorage.findAllPhotos().blockingGet()

        // Then returned photos in database is equal to given photos list
        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index]))
        }
    }

    @Test
    fun given_photo_local_storage_source_when_find_photo_by_id_then_found_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Bitmap = photoLocalStorage.findPhotoById(photo.id).blockingGet()
        assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhoto))
    }

    @Test
    fun given_photo_local_storage_source_when_find_photos_by_ids_then_found_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()
        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        val expectedPhotos: List<Bitmap> = photoLocalStorage.findPhotosByIds(photoIds).blockingGet()

        assertThat(BitmapUtil.sameAs(fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap!!,
            expectedPhotos[0]))
        assertThat(BitmapUtil.sameAs(fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap!!,
            expectedPhotos[1]))
    }

    @Test
    fun given_photo_local_storage_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
        }
        photoLocalStorage.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = photoLocalStorage.findPhotoById(initialPhoto.id).blockingGet()

        assertThat(BitmapUtil.sameAs(finalPhoto, updatedPhoto.bitmap!!))
    }

    @Test
    fun given_photo_local_storage_source_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
            }
        }
        photoLocalStorage.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = photoLocalStorage.findPhotosByIds(ids).blockingGet()

        finalPhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo, updatedPhotos[index].bitmap!!))
        }
    }

    @Test
    fun given_photo_local_storage_source_when_delete_photo_by_id_then_deleted_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        assertThat(photoLocalStorage.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoLocalStorage.deletePhotoById(photo.id).blockingAwait()
        assertThat(photoLocalStorage.findAllPhotos().blockingGet().contains(photo.bitmap))
            .isFalse()
    }

    @Test
    fun given_photo_local_storage_source_when_delete_photos_by_ids_then_deleted_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        photoLocalStorage.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = photoLocalStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap))
            .isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap))
            .isFalse()
    }

    @Test
    fun given_photo_local_storage_source_when_delete_photos_then_deleted_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoLocalStorage.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        photoLocalStorage.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = photoLocalStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_photo_local_storage_source_when_delete_all_photos_then_deleted_successfully() {
        photoLocalStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoLocalStorage.findAllPhotos().blockingGet().size
        ).isEqualTo(fakePhotos.size)
        photoLocalStorage.deleteAllPhotos().blockingAwait()
        assertThat(photoLocalStorage.findAllPhotos().blockingGet()).isEmpty()
    }

    private fun bitmapFromAsset(fileName: String): Bitmap {
        val inputStream = assets.open(fileName + Constants.SLASH + Constants.THUMBNAIL_FILE_NAME)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap
    }

    companion object {
        const val firstPropertyId: String = "2orYJD9m1aAPbTKcrkBj"
        const val secondPropertyId: String = "AMEs0idV3ur4eqK2vF3O"
    }
}