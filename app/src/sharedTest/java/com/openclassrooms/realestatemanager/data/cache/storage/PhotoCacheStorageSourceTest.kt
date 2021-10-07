package com.openclassrooms.realestatemanager.data.cache.storage

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
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.BitmapUtil.bitmapFromAsset
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoCacheStorageSourceTest : TestCase() {

    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var resources: Resources
    private lateinit var assets: AssetManager

    private lateinit var photoCacheStorage: PhotoCacheStorageSource

    @Before
    public override fun setUp() {
        super.setUp()
        assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        photoCacheStorage = PhotoCacheStorageSource(cacheDir =
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
        fakePhotos.forEach { photo -> photo.bitmap = bitmapFromAsset(
            InstrumentationRegistry.getInstrumentation().targetContext,
            photo.id) }
    }

    @After
    public override fun tearDown() {
        if(photoCacheStorage.count().blockingGet() != 0) {
            photoCacheStorage.deleteAllPhotos().blockingAwait()
        }
        super.tearDown()
    }

    @Test
    fun given_cache_storage_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoCacheStorage.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_cache_storage_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(photoCacheStorage.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoCacheStorage.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_cache_storage_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoCacheStorage.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        val expectedPhoto = photoCacheStorage.findPhotoById(fakePhotos[0].propertyId, fakePhotos[0].id).blockingGet()
        assertThat(BitmapUtil.sameAs(fakePhotos[0].bitmap!!, expectedPhoto)).isTrue()
    }

    @Test
    fun given_cache_storage_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoCacheStorage.findAllPhotos().blockingGet()

        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index])).isTrue()
        }
    }

    @Test
    fun given_cache_storage_source_when_find_all_photos_then_found_successfully() {
        // Given photos list and When photos list saved
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        val expectedPhotos = photoCacheStorage.findAllPhotos().blockingGet()

        // Then returned photos in database is equal to given photos list
        fakePhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhotos[index])).isTrue()
        }
    }

    @Test
    fun given_cache_storage_source_when_find_photo_by_id_then_found_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Bitmap = photoCacheStorage.findPhotoById(photo.propertyId,photo.id).blockingGet()
        assertThat(BitmapUtil.sameAs(photo.bitmap!!, expectedPhoto)).isTrue()
    }

    @Test
    fun given_cache_storage_source_when_find_photos_by_ids_then_found_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()
        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        val expectedPhotos: List<Bitmap> = photoCacheStorage.findPhotosByIds(photoIds).blockingGet()

        assertThat(BitmapUtil.sameAs(
            fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap!!,
            expectedPhotos[0])
        ).isTrue()
        assertThat(BitmapUtil.sameAs(
            fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap!!,
            expectedPhotos[1])
        ).isTrue()
    }

    @Test
    fun given_cache_storage_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
        }
        photoCacheStorage.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = photoCacheStorage.findPhotoById(initialPhoto.propertyId, initialPhoto.id).blockingGet()

        assertThat(BitmapUtil.sameAs(finalPhoto, updatedPhoto.bitmap!!)).isTrue()
    }

    @Test
    fun given_cache_storage_source_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
            }
        }
        photoCacheStorage.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = photoCacheStorage.findPhotosByIds(ids).blockingGet()

        finalPhotos.forEachIndexed { index, photo ->
            assertThat(BitmapUtil.sameAs(photo, updatedPhotos[index].bitmap!!)).isTrue()
        }
    }

    @Test
    fun given_cache_storage_source_when_delete_photo_by_id_then_deleted_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        assertThat(photoCacheStorage.count().blockingGet()).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoCacheStorage.deletePhotoById(photo.id).blockingAwait()
        assertThat(photoCacheStorage.findAllPhotos().blockingGet().contains(photo.bitmap))
            .isFalse()
    }

    @Test
    fun given_cache_storage_source_when_delete_photos_by_ids_then_deleted_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        photoCacheStorage.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = photoCacheStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] }.bitmap))
            .isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] }.bitmap))
            .isFalse()
    }

    @Test
    fun given_cache_storage_source_when_delete_photos_then_deleted_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoCacheStorage.count().blockingGet()).isEqualTo(fakePhotos.size)

        photoCacheStorage.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = photoCacheStorage.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_cache_storage_source_when_delete_all_photos_then_deleted_successfully() {
        photoCacheStorage.savePhotos(fakePhotos).blockingAwait()
        assertThat(photoCacheStorage.count().blockingGet()
        ).isEqualTo(fakePhotos.size)
        photoCacheStorage.deleteAllPhotos().blockingAwait()
        assertThat(photoCacheStorage.findAllPhotos().blockingGet()).isEmpty()
    }

    companion object {
        const val firstPropertyId: String = "2orYJD9m1aAPbTKcrkBj"
        const val secondPropertyId: String = "AMEs0idV3ur4eqK2vF3O"
    }
}