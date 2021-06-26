package com.openclassrooms.realestatemanager.data.cache.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.util.ConstantsTest.PHOTOS_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class PhotoCacheDataSourceTest : TestCase() {

    private lateinit var database: AppDatabase
    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>

    private lateinit var cacheDataSource: PhotoCacheDataSource

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

        cacheDataSource = PhotoCacheDataSource(database.photoDao())
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun given_cache_data_source_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheDataSource.count().blockingGet()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_cache_data_source_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        val firstPropertyId = UUID.randomUUID().toString()
        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = firstPropertyId
        }

        val secondPropertyId = UUID.randomUUID().toString()
        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = secondPropertyId
        }

        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheDataSource.count(firstPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(cacheDataSource.count(secondPropertyId).blockingGet())
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_cache_data_source_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        cacheDataSource.savePhoto(fakePhotos[0]).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheDataSource.findPhotoById(fakePhotos[0].id).blockingGet()).isEqualTo(fakePhotos[0])
    }

    @Test
    fun given_cache_data_source_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        // Then count of photos in database is equal to given photos list size
        assertThat(cacheDataSource.findAllPhotos().blockingGet()).isEqualTo(fakePhotos)
    }

    @Test
    fun given_cache_data_source_when_find_all_photos_then_found_successfully() {

        // Given photos list
        fakePhotos = fakePhotos.sortedBy { it.id }

        // When photos list saved
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        var actualPhotos = cacheDataSource.findAllPhotos().blockingGet()

        // Then returned photos in database is equal to given photos list
        actualPhotos = actualPhotos.sortedBy { it.id }
        actualPhotos.forEachIndexed { index, photo ->
            assertThat(photo).isEqualTo(fakePhotos[index])
        }
    }

    @Test
    fun given_cache_data_source_when_find_photo_by_id_then_found_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = cacheDataSource.findPhotoById(photo.id).blockingGet()
        assertThat(expectedPhoto).isEqualTo(photo)
    }

    @Test
    fun given_cache_data_source_when_find_photos_by_ids_then_found_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        val photoIds = fakePhotos.subList(0, 2).map { photo -> photo.id }
        val expectedPhotos: List<Photo> = cacheDataSource.findPhotosByIds(photoIds).blockingGet()
        assertThat(expectedPhotos).isEqualTo(fakePhotos.subList(0, 2))
    }

    @Test
    fun given_cache_data_source_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = PhotoType.values().first { type -> type != initialPhoto.type }
        }
        cacheDataSource.updatePhoto(updatedPhoto).blockingAwait()

        val finalPhoto = cacheDataSource.findPhotoById(initialPhoto.id).blockingGet()
        assertThat(finalPhoto).isEqualTo(updatedPhoto)
    }

    @Test
    fun given_cache_data_source_when_update_photos_then_updated_successfully() {
        var initialPhotos = arrayOf(fakePhotos[0], fakePhotos[1])

        cacheDataSource.savePhotos(fakePhotos).blockingAwait()

        var updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = PhotoType.values().first { type -> type != initialPhotos[index].type }
            }
        }
        updatedPhotos = updatedPhotos.sortedBy { it.id }
        cacheDataSource.updatePhotos(updatedPhotos).blockingAwait()

        val ids = initialPhotos.map { photo -> photo.id }
        var finalPhotos = cacheDataSource.findAllPhotos().blockingGet().filter {
                photo -> ids.contains(photo.id)
        }
        finalPhotos = finalPhotos.sortedBy { it.id }

        assertThat(finalPhotos).isEqualTo(updatedPhotos.toList())
    }

    @Test
    fun given_cache_data_source_when_delete_photo_by_id_then_deleted_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        val photo = fakePhotos[fakePhotos.indices.random()]
        cacheDataSource.deletePhotoById(photo.id).blockingAwait()
        assertThat(cacheDataSource.findAllPhotos().blockingGet().contains(photo))
            .isFalse()
    }

    @Test
    fun given_cache_data_source_when_delete_photos_by_ids_then_deleted_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        val photoIds = fakePhotos.subList(0, 2).map { photo -> photo.id }
        cacheDataSource.deletePhotosByIds(photoIds).blockingAwait()

        val findAllPhotos = cacheDataSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.containsAll(fakePhotos.subList(0, 2))).isFalse()
    }

    @Test
    fun given_cache_data_source_when_delete_photos_then_deleted_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(cacheDataSource.findAllPhotos().blockingGet().size).isEqualTo(fakePhotos.size)

        cacheDataSource.deletePhotos(fakePhotos.subList(0, 2)).blockingAwait()

        val findAllPhotos = cacheDataSource.findAllPhotos().blockingGet()
        assertThat(findAllPhotos.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_cache_data_source_when_delete_all_photos_then_deleted_successfully() {
        cacheDataSource.savePhotos(fakePhotos).blockingAwait()
        assertThat(
            cacheDataSource.findAllPhotos().blockingGet().size
        ).isEqualTo(fakePhotos.size)
        cacheDataSource.deleteAllPhotos().blockingAwait()
        assertThat(cacheDataSource.findAllPhotos().blockingGet()).isEmpty()
    }
}