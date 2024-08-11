package com.comp304.lab4.data

import android.content.Context
import android.content.pm.PackageManager
import com.comp304.lab4.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader

class LandmarkReader(private val context: Context) {

    private val gson = Gson()

    private val inputStream: InputStream
        get() = context.resources.openRawResource(R.raw.landmarks)

    fun read(): List<Landmark> {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark()
        }
    }

    fun read(landmarkType: String): List<Landmark> {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark()
        }
            .filter { it.landmarkType.equals(landmarkType, ignoreCase = true)}
    }


    fun readSingle(name: String): Landmark? {
        val itemType = object : TypeToken<List<LandmarkResponse>>() {}.type
        val reader = InputStreamReader(inputStream)
        return gson.fromJson<List<LandmarkResponse>>(reader, itemType).map {
            it.toLandmark() }
            .find {
                it.name.equals(name, ignoreCase = true) }
    }
}