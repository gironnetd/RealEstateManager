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
import com.openclassrooms.realestatemanager.models.Picture
import com.openclassrooms.realestatemanager.util.ConstantsTest.PICTURES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class PictureDaoTest: TestCase() {

    lateinit var database: AppDatabase
    private lateinit var pictureDao: PictureDao
    lateinit var jsonUtil: JsonUtil
    private lateinit var fakePictures: List<Picture>

    @Before
    fun initDatabase() {

        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
        ).allowMainThreadQueries().build()

        jsonUtil = JsonUtil()
        var rawJson = jsonUtil.readJSONFromAsset(PICTURES_DATA_FILENAME)
        fakePictures = Gson().fromJson(
                rawJson,
                object : TypeToken<List<Picture>>() {}.type
        )

        pictureDao = database.pictureDao()
    }

    @After
    fun clearDatabase() = database.clearAllTables()

    @Test
    fun insert_pictures_with_success() {
        pictureDao.savePictures(fakePictures)
        assertThat(pictureDao.count()).isEqualTo(fakePictures.size)
    }

    @Test
    fun is_pictures_after_insertion_are_the_same_when_reading_result() {
        fakePictures = fakePictures.sortedBy { it.id }
        pictureDao.savePictures(fakePictures)
        val cursor = pictureDao.findAllPictures()

        var actualPictures = cursor.toList {
            Picture(it)
        }

        actualPictures = actualPictures.sortedBy { it.id }
        actualPictures.forEachIndexed { index, picture ->
            assertThat(picture).isEqualTo(fakePictures[index])
        }
    }
}