package com.comp304.lab4

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.comp304.lab4.adapters.LandmarkRecyclerViewAdapter
import com.comp304.lab4.data.LandmarkReader
import com.comp304.lab4.util.Constants.LANDMARKTYPE_KEY
import com.google.android.libraries.places.api.Places

class LandmarkList : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var landmarkAdapter: LandmarkRecyclerViewAdapter
    private lateinit var errorLayout: View
    private lateinit var errorMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landmark_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        errorLayout = findViewById(R.id.errorLayout)
        errorMessage = findViewById(R.id.errorMessage)
        val titleTextView: TextView = findViewById(R.id.textViewTitleLandmarks)

        // Retrieve the landmarkType from the intent
        val landmarkType = intent.getStringExtra(LANDMARKTYPE_KEY)!!
        titleTextView.text = landmarkType

        // Find the RecyclerView in the layout
        recyclerView = findViewById(R.id.recyclerViewLandmarks)
        recyclerView.layoutManager = LinearLayoutManager(this)


        try {
            // Initialize the adapter with the context and the landmarkType
            val landmarks = LandmarkReader(this).read(landmarkType)

            if (landmarks.isNotEmpty()) {
                // Set the adapter to the RecyclerView if data is available
                landmarkAdapter = LandmarkRecyclerViewAdapter(this, landmarkType)
                recyclerView.adapter = landmarkAdapter
            } else {
                // Handle the case where no landmarks were found
                showError("No landmarks found for this type.")
            }
        } catch (e: Exception) {
            // Handle any exceptions that occur during data loading
            showError("Failed to load landmarks: ${e.message}")
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

    private fun showError(message: String) {
        // Show error message and hide the RecyclerView
        recyclerView.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorMessage.text = message
    }
}