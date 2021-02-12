package com.openclassrooms.realestatemanager.api.property

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.remote.PropertyApiService
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.Constants
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FakePropertyApiService
@Inject
constructor(
        private val jsonUtil: JsonUtil,
) : PropertyApiService {
    var propertiesJsonFileName: String = Constants.PROPERTIES_DATA_FILENAME
    var networkDelay: Long = 0L

    override fun insertProperties(properties: List<Property>): Completable {
        TODO("Not yet implemented")
    }

    override fun findAllProperties(): Flowable<List<Property>> {
        val rawJson = jsonUtil.readJSONFromAsset(propertiesJsonFileName)
        val properties = Gson().fromJson<List<Property>>(
                rawJson,
                object : TypeToken<List<Property>>() {}.type
        )
        return Flowable.just(properties).delay(networkDelay, TimeUnit.MILLISECONDS)
    }
}