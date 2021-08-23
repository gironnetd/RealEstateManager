package com.openclassrooms.realestatemanager.data.fake.property

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openclassrooms.realestatemanager.data.source.property.PropertyDataSource
import com.openclassrooms.realestatemanager.di.property.browse.BrowseScope
import com.openclassrooms.realestatemanager.models.Property
import com.openclassrooms.realestatemanager.util.ConstantsTest
import com.openclassrooms.realestatemanager.util.JsonUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@BrowseScope
class FakePropertyDataSource
@Inject constructor(var jsonUtil: JsonUtil?): PropertyDataSource {

    var propertiesJsonFileName: String = ConstantsTest.PROPERTIES_DATA_FILENAME
    var properties: ArrayList<Property> = arrayListOf()

    init {
        val rawJson = jsonUtil!!.readJSONFromAsset(propertiesJsonFileName)
        properties = Gson().fromJson(rawJson, object : TypeToken<List<Property>>() {}.type)
        properties.toList().sortedBy { it.id }
    }

    override fun count(): Single<Int> {
        return Single.just(properties.size)
    }

    override fun saveProperty(property: Property): Completable {
        return Completable.fromAction { properties.add(property) }
    }

    override fun saveProperties(properties: List<Property>): Completable {
        return Completable.fromAction { this.properties.addAll(properties) }
    }

    override fun findPropertyById(id: String): Single<Property> {
        return Single.just(properties.single { property -> property.id == id })
    }

    override fun findPropertiesByIds(ids: List<String>): Single<List<Property>> {
        return Observable.fromIterable(ids).flatMapSingle { id ->
            findPropertyById(id)
        }.toList().flatMap { properties ->
            Single.just(properties)
        }
    }

    override fun findAllProperties(): Single<List<Property>> {
        return Single.just(properties)
    }

    override fun updateProperty(property: Property): Completable {
        return Completable.fromAction {
            val actualProperty = properties.single { it.id == property.id }
            properties.remove(actualProperty)
            properties.add(property)
        }
    }

    override fun updateProperties(properties: List<Property>): Completable {
        return Observable.fromIterable(properties).flatMapCompletable { property ->
           updateProperty(property)
        }
    }

    override fun deletePropertiesByIds(ids: List<String>): Completable {
        return Completable.fromAction { ids.forEach { id -> deletePropertyById(id) } }
    }

    override fun deleteProperties(properties: List<Property>): Completable {
        return Completable.fromAction { properties.forEach { property -> this.properties.remove(property) } }
    }

    override fun deleteAllProperties(): Completable {
        return Completable.create { emitter ->
            properties.clear()
            emitter.onComplete()
        }
    }

    override fun deletePropertyById(id: String): Completable {
        return Completable.fromAction { properties.remove(properties.single { property -> property.id == id }) }
    }
}