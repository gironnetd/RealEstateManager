package com.openclassrooms.realestatemanager.data.cache.source

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.data.cache.data.PhotoCacheDataSource
import com.openclassrooms.realestatemanager.data.cache.storage.PhotoCacheStorageSource
import com.openclassrooms.realestatemanager.data.cache.storage.PhotoCacheStorageSourceTest
import com.openclassrooms.realestatemanager.models.property.Photo
import com.openclassrooms.realestatemanager.util.BitmapUtil
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoCacheSourceTest : TestCase() {

    private lateinit var database: AppDatabase
    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>
    private lateinit var resources: Resources

    private lateinit var photoCacheData: PhotoCacheDataSource
    private lateinit var photoCacheStorage: PhotoCacheStorageSource

    private lateinit var cacheSource: PhotoCacheSource

    @Before
    public override fun setUp() {
        super.setUp()
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java).allowMainThreadQueries().build()

        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(ConstantsTest.PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = PhotoCacheStorageSourceTest.firstPropertyId
        }

        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = PhotoCacheStorageSourceTest.secondPropertyId
        }

        fakePhotos = fakePhotos.sortedBy { it.id }
        fakePhotos.forEach { photo -> photo.bitmap = BitmapUtil.bitmapFromAsset(
            InstrumentationRegistry.getInstrumentation().targetContext,
            photo.id)
        }

        photoCacheData = PhotoCacheDataSource(photoDao = database.photoDao())

        photoCacheStorage = PhotoCacheStorageSource(cacheDir =
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir)

        cacheSource = PhotoCacheSource(cacheData = photoCacheData, cacheStorage = photoCacheStorage)

        if(cacheSource.count().blockingGet() != 0) {
            cacheSource.deleteAllPhotos().blockingAwait()
        }
    }

    @After
    public override fun tearDown() {
        if(cacheSource.count().blockingGet() != 0) {
            cacheSource.deleteAllPhotos().blockingAwait()
        }
        super.tearDown()
    }

    @Test
    fun given_cache_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheSource.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_cache_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheSource.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(cacheSource.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_cache_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        cacheSource.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertSameAs(actual = fakePhotos[0],expected = cacheSource.findPhotoById(fakePhotos[0].propertyId, fakePhotos[0].id).blockingGet())
    }

    @Test
    fun given_cache_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        fakePhotos = fakePhotos.sortedBy { it.id }
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        val expectedPhotos = cacheSource.findAllPhotos().blockingGet().sortedBy { it.id }
        fakePhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = expectedPhotos[index])
        }
    }

    @Test
    fun given_cache_source_when_find_all_photos_then_found_successfully() {
        // Given photos list and When photos list saved
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        // Then returned photos in database is equal to given photos list
        val expectedPhotos = cacheSource.findAllPhotos().blockingGet().sortedBy { it.id }

        fakePhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = expectedPhotos[index])
        }
    }

    @Test
    fun given_cache_source_when_find_photo_by_id_then_found_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = cacheSource.findPhotoById(photo.propertyId, photo.id).blockingGet()
        assertSameAs(actual = photo,expected = expectedPhoto)
    }

    @Test
    fun given_cache_source_when_find_photos_by_ids_then_found_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()
        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        var expectedPhotos: List<Photo> = cacheSource.findPhotosByIds(photoIds).blockingGet()

        expectedPhotos = expectedPhotos.sortedBy { it.id }
        assertSameAs(actual = fakePhotos.single { photo -> photo.id == photoIds[0] },expected = expectedPhotos[0])
        assertSameAs(actual = fakePhotos.single { photo -> photo.id == photoIds[1] },expected = expectedPhotos[1])
    }

    @Test
    fun given_cache_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        cacheSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = com.openclassrooms.realestatemanager.models.property.PhotoType.values().first { type -> type != initialPhoto.type }
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
        }
        cacheSource.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = cacheSource.findPhotoById(initialPhoto.propertyId, initialPhoto.id).blockingGet()
        assertSameAs(actual = updatedPhoto,expected = finalPhoto)
    }

    @Test
    fun given_cache_source_when_update_photos_then_updated_successfully() {
        val initialPhotos = arrayOf(fakePhotos[0], fakePhotos[fakePhotos.indices.count() / 2])

        cacheSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = com.openclassrooms.realestatemanager.models.property.PhotoType.values().first { type -> type != initialPhotos[index].type }
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_image)
            }
        }
        cacheSource.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        val finalPhotos = cacheSource.findAllPhotos().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }

        updatedPhotos.forEachIndexed { index, photo ->
            assertSameAs(actual = photo, expected = finalPhotos[index])
        }
    }

    @Test
    fun given_cache_source_when_delete_photo_by_id_then_deleted_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        assertThat(cacheSource.count().blockingGet()).isEqualTo(fakePhotos.size)
        val photo = fakePhotos[fakePhotos.indices.random()]
        cacheSource.deletePhotoById(photo.id).blockingAwait()
        assertThat(cacheSource.findAllPhotos().blockingGet().contains(photo))
            .isFalse()
    }

    @Test
    fun given_cache_source_when_delete_photos_by_ids_then_deleted_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()

        val photoIds: MutableList<String> = mutableListOf()
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == firstPropertyId }.id)
        photoIds.add(fakePhotos.first { photo -> photo.propertyId == secondPropertyId }.id)

        cacheSource.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = cacheSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[0] })).isFalse()
        assertThat(findAllPhotos.contains(fakePhotos.single { photo -> photo.id == photoIds[1] })).isFalse()
    }

    @Test
    fun given_cache_source_when_delete_photos_then_deleted_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(cacheSource.count().blockingGet()).isEqualTo(fakePhotos.size)

        cacheSource.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = cacheSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_cache_source_when_delete_all_photos_then_deleted_successfully() {
        cacheSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(cacheSource.count().blockingGet()).isEqualTo(fakePhotos.size)
        cacheSource.deleteAllPhotos().blockingAwait()
        assertThat(cacheSource.findAllPhotos().blockingGet()).isEmpty()
    }

    private fun assertSameAs(actual: Photo, expected: Photo) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.propertyId).isEqualTo(expected.propertyId)
        assertThat(actual.mainPhoto).isEqualTo(expected.mainPhoto)
        assertThat(actual.description).isEqualTo(expected.description)
        assertThat(actual.type).isEqualTo(expected.type)
        assertThat(BitmapUtil.sameAs(actual.bitmap!!, expected.bitmap!!))
    }

    companion object {
        const val firstPropertyId: String = "2orYJD9m1aAPbTKcrkBj"
        const val secondPropertyId: String = "AMEs0idV3ur4eqK2vF3O"
    }
}