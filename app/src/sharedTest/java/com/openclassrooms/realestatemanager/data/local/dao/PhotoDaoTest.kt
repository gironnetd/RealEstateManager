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

    lateinit var database: AppDatabase
    private lateinit var photoDao: PhotoDao
    lateinit var jsonUtil: JsonUtil
    private lateinit var fakePhotos: List<Photo>

    @Before
    fun initDatabase() {

        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        var rawJson = jsonUtil.readJSONFromAsset(PHOTOS_DATA_FILENAME)
        fakePhotos = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)
        photoDao = database.photoDao()
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun given_photos_when_saved_then_photos_are_inserted_with_success() {
        // Given photos list
        // When photos list saved
        photoDao.savePhotos(fakePhotos)

        // Then count of photos in database is equal to given photos list size
        assertThat(photoDao.count()).isEqualTo(fakePhotos.size)
    }

    @Test
    fun given_photos_when_saved_then_reading_result_is_equal_to() {
        // Given photos list
        fakePhotos = fakePhotos.sortedBy { it.id }

        // When photos list saved
        photoDao.savePhotos(fakePhotos)

        val cursor = photoDao.findAllPhotos()

        var actualPhotos = cursor.toList {
            Photo(it)
        }

        // Then returned photos in database is equal to given photos list
        actualPhotos = actualPhotos.sortedBy { it.id }
        actualPhotos.forEachIndexed { index, photo ->
            assertThat(photo).isEqualTo(fakePhotos[index])
        }
    }
}