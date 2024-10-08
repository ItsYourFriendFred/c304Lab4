package com.comp304.lab4

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.comp304.lab4.adapters.MarkerInfoWindowAdapter
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.comp304.lab4.util.Constants.LANDMARKLATLNG_KEY
import com.comp304.lab4.util.Constants.LANDMARKNAME_KEY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
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

    private lateinit var googleMap: GoogleMap

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

        // Get reference to Map fragment UI component
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        // Launch a coroutine to handle the asynchronous GoogleMap instance calls
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                // Get map
                googleMap = mapFragment.awaitMap()

                configureDefaultMapSettings(googleMap)

                // Set the custom InfoWindowAdapter
                googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this@LandmarkMap))

                // Wait for map to finish loading
                googleMap.awaitMapLoad()

                // Move camera to the chosen Landmark
                latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 14f) }
                    ?.let { googleMap.moveCamera(it) }

                addMarker(googleMap)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_normal_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.menu_satellite_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.menu_hybrid_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.menu_terrain_map -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Add marker to map and show it immediately as if clicked
    private fun addMarker(googleMap: GoogleMap) {
        latLng?.let {
            googleMap.addMarker(
                MarkerOptions()
                    .title(name)
                    .position(it)
            )?.apply {
                tag = landmark
                showInfoWindow()
            }
        }
    }

    // Set default UI settings of Google Map object
    private fun configureDefaultMapSettings(googleMap: GoogleMap) {
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }
}