package com.openclassrooms.realestatemanager.util

import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class for parsing data from fake data assets
 */
@Singleton
public class JsonUtil
@Inject
constructor() {
    fun readJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream: InputStream = javaClass.classLoader!!.getResourceAsStream(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }
}