package com.comp304.lab4.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.comp304.lab4.R
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest




internal class LandmarkRecyclerViewAdapter(
    private val context: Context,
    private val landmarkType: String
) : RecyclerView.Adapter<LandmarkRecyclerViewAdapter.MyViewHolder>() {

    private val landmarks: List<Landmark> by lazy {
        LandmarkReader(context).read(landmarkType)
    }

    private val placesClient: PlacesClient = Places.createClient(context)

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardLandmark)
        private val landmarkNameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val landmarkAddressTextView: TextView = itemView.findViewById(R.id.textViewAddress)
        private val landmarkRatingTextView: TextView = itemView.findViewById(R.id.textViewRating)

        private val placeFields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME)

        fun bind(landmark: Landmark) {
            landmarkNameTextView.text = landmark.name
            landmarkAddressTextView.text = landmark.address
            landmarkRatingTextView.text = (context.getString(R.string.recyclerView_rating_text, landmark.rating.toString()))

            val searchCenter  = LatLng(landmark.latLng.latitude, landmark.latLng.longitude)

            val searchByTextRequest =
                SearchByTextRequest.builder(landmark.name, placeFields)
                    .setMaxResultCount(10)
                    .setLocationRestriction(CircularBounds.newInstance(searchCenter, 1000.0))
                    .build()

            placesClient.searchByText(searchByTextRequest)
                .addOnSuccessListener { response ->
                    val places: List<Place> = response.places
                }



        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_landmark, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return landmarks.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = landmarks[position]
        holder.bind(currentItem)
    }
}