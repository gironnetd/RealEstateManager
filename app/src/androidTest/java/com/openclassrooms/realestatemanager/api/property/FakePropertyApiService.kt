package com.openclassrooms.realestatemanager.api.property

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.models.Property
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
    var networkDelay: Long = 0L

    var properties: List<Property> = mutableListOf()

    override fun insertProperties(properties: List<Property>): Completable {
        this.properties.toMutableList().addAll(properties)
        return Completable.complete()
    }

    override fun findAllProperties(): Single<List<Property>> {
        val rawJson = jsonUtil.readJSONFromAsset(propertiesJsonFileName)
        properties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)

        properties = properties.sortedBy { it.id }
        return Single.just(properties).delay(networkDelay, TimeUnit.MILLISECONDS)
    }
}