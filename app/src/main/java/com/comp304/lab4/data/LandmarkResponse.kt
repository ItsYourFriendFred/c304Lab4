package com.comp304.lab4.data

import com.google.android.gms.maps.model.LatLng

data class LandmarkResponse(
    val geometry: Geometry,
    val name: String,
    val vicinity: String,
    val rating: Float,
    val landmarkType: String
) {

    data class Geometry(
        val location: GeometryLocation
    )

    data class GeometryLocation(
        val lat: Double,
        val lng: Double
    )
}

fun LandmarkResponse.toLandmark(): Landmark = Landmark(
    name = name,
    latLng = LatLng(geometry.location.lat, geometry.location.lng),
    address = vicinity,
    rating = rating,
    landmarkType = landmarkType
)