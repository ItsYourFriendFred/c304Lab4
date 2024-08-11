package com.comp304.lab4

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions

class LandmarkMap : AppCompatActivity() {

    private val name: String = null.toString()
    private val landmark: Landmark? by lazy {
        LandmarkReader(this).readSingle(name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landmark_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            addMarker(googleMap)
        }
    }

    private fun addMarker(googleMap: GoogleMap) {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .title(landmark?.name)
                .position(landmark!!.latLng)
        )
    }
}