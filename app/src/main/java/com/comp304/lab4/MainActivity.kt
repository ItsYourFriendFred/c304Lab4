package com.comp304.lab4

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comp304.lab4.adapters.LandmarkTypeRecyclerViewAdapter
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var landmarkTypeAdapter: LandmarkTypeRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the RecyclerView to display the types of landmarks
        recyclerView = findViewById(R.id.recyclerViewLandmarkTypes)
        landmarkTypeAdapter = LandmarkTypeRecyclerViewAdapter(this)
        recyclerView.adapter = landmarkTypeAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Define a variable to hold the Maps/Places API key.
        val apiKey = BuildConfig.MAPS_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            finish()
            return
        }

        // Initialize the SDK globally so it can be accessed anywhere (i.e., any activity or context)
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
    }
}