package com.openclassrooms.realestatemanager.api.property

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.models.Photo
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest.PHOTOS_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.ConstantsTest.PROPERTIES_DATA_FILENAME
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FakePropertyApiService
@Inject
constructor(
        var jsonUtil: JsonUtil,
) : PropertyApiService {
    var propertiesJsonFileName: String = PROPERTIES_DATA_FILENAME
    var photosJsonFileName: String = PHOTOS_DATA_FILENAME
    var networkDelay: Long = 0L

    var properties: List<Property> = mutableListOf()

    override fun saveProperties(properties: List<Property>): Completable {
        this.properties.toMutableList().addAll(properties)
        return Completable.complete()
    }

    override fun findAllProperties(): Single<List<Property>> {
        var rawJson = jsonUtil.readJSONFromAsset(propertiesJsonFileName)
        properties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)

        properties = properties.sortedBy { it.id }

        rawJson =  jsonUtil.readJSONFromAsset(photosJsonFileName)

        properties.forEachIndexed { index, property ->

            property.mainPhotoId = property.id
            val photos: List<Photo> = Gson().fromJson(rawJson, object : TypeToken<List<Photo>>() {}.type)

            photos.forEach { photo ->
                photo.propertyId = property.id
            }
            properties[index].photos.addAll(photos)
        }

        return Single.just(properties).delay(networkDelay, TimeUnit.MILLISECONDS)
    }
}