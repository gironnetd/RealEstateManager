package com.openclassrooms.realestatemanager.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.local.AppDatabase
import com.openclassrooms.realestatemanager.data.local.provider.toList
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.PhotoType
import com.openclassrooms.realestatemanager.util.ConstantsTest.PHOTOS_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
    fun given_photo_dao_when_save_photos_then_saved_successfully() {

        // Given photos list and When photos list saved
        photoDao.savePhotos(fakePhotos)

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.count()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photo_dao_when_find_all_photos_then_found_successfully() {

        // Given photos list
        fakePhotos = fakePhotos.sortedBy { it.id }

        // When photos list saved
        photoDao.savePhotos(fakePhotos)

        var actualPhotos = photoDao.findAllPhotos().toList { Photo(it) }

        // Then returned photos in database is equal to given photos list
        actualPhotos = actualPhotos.sortedBy { it.id }
        actualPhotos.forEachIndexed { index, photo ->
            assertThat(photo).isEqualTo(fakePhotos[index])
        }
    }

    @Test
    fun given_photo_dao_when_find_photo_by_id_then_found_successfully() {
        photoDao.savePhotos(fakePhotos)
        val photo = fakePhotos[fakePhotos.indices.random()]
        val expectedPhoto: Photo = photoDao.findPhotoById(photo.id).toList { Photo(it) }.single()
        assertThat(expectedPhoto).isEqualTo(photo)
    }

    @Test
    fun given_photo_dao_when_one_photo_saved_then_saved_successfully() {
        // Given photo and When photo saved
        val savedPhoto: Photo = fakePhotos[fakePhotos.indices.random()]
        photoDao.savePhoto(savedPhoto)

        // Then result  of reading is equal to given properties list
        val actualPhoto = photoDao.findPhotoById(savedPhoto.id).toList { Photo(it) }
            .singleOrNull { photo ->  photo.id == savedPhoto.id }

        assertThat(actualPhoto).isNotNull()
        assertThat(actualPhoto).isEqualTo(savedPhoto)
    }

    @Test
    fun given_photo_dao_when_update_photo_then_updated_successfully() {
        val initialPhoto = fakePhotos[fakePhotos.indices.random()]

        photoDao.savePhoto(initialPhoto)

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
    fun given_photo_dao_when_delete_photo_by_id_then_deleted_successfully() {
        photoDao.savePhotos(fakePhotos)
        val photo = fakePhotos[fakePhotos.indices.random()]
        photoDao.deleteById(photo.id)
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }.contains(photo)).isFalse()
    }

    @Test
    fun given_photo_dao_when_delete_all_photos_then_deleted_successfully() {
        photoDao.savePhotos(fakePhotos)
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }.size
        ).isEqualTo(fakePhotos.size)
        photoDao.deleteAllPhotos()
        assertThat(photoDao.findAllPhotos().toList { Photo(it) }).isEmpty()
    }

}