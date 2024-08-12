package com.comp304.lab4.data

import android.content.Context
import android.content.pm.PackageManager
import com.comp304.lab4.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Reads a list of landmark JSON objects from landmarks.json in raw resource directory
 */
class LandmarkReader(private val context: Context) {

    // GSON object responsible for converting from JSON to a Landmark object
    private val gson = Gson()

    // InputStream representing landmarks.json
    private val inputStream: InputStream
        get() = context.resources.openRawResource(R.raw.landmarks)

    /**
     * Variety of methods to read the list of landmark JSON objects in landmarks.json
     * and returns a list of Landmark objects, filtered with certain criteria if applicable.
     */

    // Gets all landmarks
    fun read(): List<Landmark> {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark()
        }
    }

    // Gets all landmarks matching the landmark type passed in
    fun read(landmarkType: String): List<Landmark> {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark()
        }
            .filter { it.landmarkType.equals(landmarkType, ignoreCase = true)}
    }

    // Gets a single Landmark matching the landmark name passed in
    fun readSingle(name: String): Landmark? {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark() }
            .find {
                it.name.equals(name, ignoreCase = true) }
    }
}