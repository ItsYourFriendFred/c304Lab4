package com.comp304.lab4.data

import com.google.android.gms.maps.model.LatLng

data class Landmark(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Float,
    val landmarkType: String
)