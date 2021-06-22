package com.openclassrooms.realestatemanager.data.cache.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.cache.AppDatabase
import com.openclassrooms.realestatemanager.data.cache.provider.toList
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
@SmallTest
class PhotoDaoTest: TestCase() {

    private lateinit var database: AppDatabase
    private lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>

    private lateinit var photoDao: PhotoDao

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        val rawJson = jsonUtil.readJSONFromAsset(PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        photoDao = database.photoDao()
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun given_photo_dao_when_save_photos_then_counted_successfully() {
        // Given photos list and When photos list saved
        photoDao.savePhotos(*fakePhotos.toTypedArray())

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.count()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_dao_when_save_photos_then_counted_by_propertyId_successfully() {
        // Given photos list and When photos list saved
        val firstPropertyId = UUID.randomUUID().toString()
        fakePhotos.subList(0, fakePhotos.indices.count() / 2).forEach { photo ->
            photo.propertyId = firstPropertyId
        }

        val secondPropertyId = UUID.randomUUID().toString()
        fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).forEach { photo ->
            photo.propertyId = secondPropertyId
        }

        photoDao.savePhotos(*fakePhotos.toTypedArray())

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.count(firstPropertyId))
            .isEqualTo(fakePhotos.subList(0, fakePhotos.indices.count() / 2).size)

        assertThat(photoDao.count(secondPropertyId))
            .isEqualTo(fakePhotos.subList(fakePhotos.indices.count() / 2, fakePhotos.indices.count()).size)
    }

    @Test
    fun given_photo_dao_when_save_a_photo_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoDao.savePhoto(fakePhotos[0])

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.findPhotoById(fakePhotos[0].id).toList { Photo(it) }.single())
            .isEqualTo(fakePhotos[0])
    }

    @Test
    fun given_photo_dao_when_save_photos_then_saved_successfully() {
        // Given photos list and When photos list saved
        photoDao.savePhotos(*fakePhotos.toTypedArray())

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }).isEqualTo(fakePhotos)
    }

    @Test
    fun given_photo_dao_when_find_all_photos_then_found_successfully() {

        // Given photos list
        fakePhotos = fakePhotos.sortedBy { it.id }

        // When photos list saved
        photoDao.savePhotos(*fakePhotos.toTypedArray())

        var actualPhotos = photoDao.findAllPhotos().toList { Photo(it) }

        // Then returned photos in database is equal to given photos list
        actualPhotos = actualPhotos.sortedBy { it.id }
        actualPhotos.forEachIndexed { index, photo ->
            assertThat(photo).isEqualTo(fakePhotos[index])
        }
    }

    @Test
    fun given_photo_dao_when_find_photo_by_id_then_found_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = photoDao.findPhotoById(photo.id).toList { Photo(it) }.single()
        assertThat(expectedPhoto).isEqualTo(photo)
    }

    @Test
    fun given_photo_dao_when_find_photos_by_ids_then_found_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        val photoIds = fakePhotos.subList(0, 2).map { photo -> photo.id }
        val expectedPhotos: List<Photo> = photoDao.findPhotosByIds(photoIds)
        assertThat(expectedPhotos).isEqualTo(fakePhotos.subList(0, 2))
    }

    @Test
    fun given_photo_dao_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoDao.savePhotos(*fakePhotos.toTypedArray())

        val updatedPhoto = initialPhoto.copy()
        with(updatedPhoto) {
            description = "new description"
            type = PhotoType.values().first { type -> type != initialPhoto.type }
        }
        photoDao.updatePhoto(updatedPhoto)

        val finalPhoto = photoDao.findPhotoById(initialPhoto.id).toList { Photo(it) }.single()
        assertThat(finalPhoto).isEqualTo(updatedPhoto)
    }

    @Test
    fun given_photo_dao_when_update_photos_then_updated_successfully() {
        var initialPhotos = arrayOf(fakePhotos[0], fakePhotos[1])

        photoDao.savePhotos(*fakePhotos.toTypedArray())

        var updatedPhotos = initialPhotos.copyOf().toList()
        updatedPhotos.forEachIndexed { index,  updatedPhoto ->
            with(updatedPhoto) {
                description = "new description"
                type = PhotoType.values().first { type -> type != initialPhotos[index].type }
            }
        }
        updatedPhotos = updatedPhotos.sortedBy { it.id }
        photoDao.updatePhotos(*updatedPhotos.toTypedArray())

        val ids = initialPhotos.map { photo -> photo.id }
        var finalPhotos = photoDao.findAllPhotos().toList { Photo(it) }.filter {
                photo -> ids.contains(photo.id)
        }
        finalPhotos = finalPhotos.sortedBy { it.id }

        assertThat(finalPhotos).isEqualTo(updatedPhotos.toList())
    }

    @Test
    fun given_photo_dao_when_delete_photo_by_id_then_deleted_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoDao.deletePhotoById(photo.id)
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }.contains(photo))
            .isFalse()
    }

    @Test
    fun given_photo_dao_when_delete_photos_by_ids_then_deleted_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        val photoIds = fakePhotos.subList(0, 2).map { photo -> photo.id }
        photoDao.deletePhotosByIds(photoIds)

        val findAllPhotos = photoDao.findAllPhotos()
        assertThat(findAllPhotos.toList { Photo(it) }.size).isEqualTo((fakePhotos.size - 2))
        assertThat(findAllPhotos.toList { Photo(it) }.containsAll(fakePhotos.subList(0, 2))).isFalse()
    }

    @Test
    fun given_photo_dao_when_delete_photos_then_deleted_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }.size).isEqualTo(fakePhotos.size)

        photoDao.deletePhotos(*fakePhotos.subList(0, 2).toTypedArray())

        val findAllPhotos = photoDao.findAllPhotos()
        assertThat(findAllPhotos.toList { Photo(it) }.size).isEqualTo((fakePhotos.size - 2))
    }

    @Test
    fun given_photo_dao_when_delete_all_photos_then_deleted_successfully() {
        photoDao.savePhotos(*fakePhotos.toTypedArray())
        assertThat(
            photoDao.findAllPhotos().toList { Photo(it) }.size
        ).isEqualTo(fakePhotos.size)
        photoDao.deleteAllPhotos()
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }).isEmpty()
    }
}