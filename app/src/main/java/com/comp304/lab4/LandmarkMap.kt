package com.comp304.lab4

import android.graphics.Camera
import android.os.Build
import android.os.Build.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.comp304.lab4.util.Constants.LANDMARKLATLNG_KEY
import com.comp304.lab4.util.Constants.LANDMARKNAME_KEY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import kotlinx.coroutines.launch

class LandmarkMap : AppCompatActivity() {

    private val name: String?
        get() {
            return intent.getStringExtra(LANDMARKNAME_KEY)
        }
    private val landmark: Landmark? by lazy {
        name?.let { LandmarkReader(this).readSingle(it) }
    }
    private val latLng: LatLng?
        get() {
            return if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra (LANDMARKLATLNG_KEY, LatLng::class.java)
            }else{
                intent.getParcelableExtra(LANDMARKLATLNG_KEY)
            }
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                // Get map
                val googleMap = mapFragment.awaitMap()

                googleMap.uiSettings.isMapToolbarEnabled = true
                googleMap.uiSettings.isCompassEnabled = true
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

                // Wait for map to finish loading
                googleMap.awaitMapLoad()

                latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }
                    ?.let { googleMap.moveCamera(it) }

                addMarker(googleMap)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addMarker(googleMap: GoogleMap) {
        val marker = latLng?.let {
            MarkerOptions()
                .title(name)
                .position(it)
        }?.let {
            googleMap.addMarker(
                it
            )
        }
    }
}