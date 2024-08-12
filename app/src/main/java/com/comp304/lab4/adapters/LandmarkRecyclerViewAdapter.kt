package com.comp304.lab4.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.comp304.lab4.LandmarkMap
import com.comp304.lab4.R
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.comp304.lab4.util.Constants.LANDMARKLATLNG_KEY
import com.comp304.lab4.util.Constants.LANDMARKNAME_KEY
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest


internal class LandmarkRecyclerViewAdapter(
    private val context: Context,
    private val landmarkType: String
) : RecyclerView.Adapter<LandmarkRecyclerViewAdapter.MyViewHolder>() {

    // Get list of landmarks based on selected landmark type from Main activity
    private val landmarks: List<Landmark> by lazy {
        LandmarkReader(context).read(landmarkType)
    }

    // Create a Places API client
    private val placesClient: PlacesClient = Places.createClient(context)

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Set references to the UI components
        private val cardView: CardView = itemView.findViewById(R.id.cardLandmark)
        private val landmarkNameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val landmarkAddressTextView: TextView = itemView.findViewById(R.id.textViewAddress)
        private val landmarkRatingTextView: TextView = itemView.findViewById(R.id.textViewRating)
        private val landmarkImageView: ImageView = itemView.findViewById(R.id.imageViewLandmarkPreview)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        // Specify list of fields to return from the Places (New) API
        private val placeFields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        fun bind(landmark: Landmark) {
            // Bind the retrieved Landmark fields to their corresponding UI components in the RecyclerView's card
            landmarkNameTextView.text = landmark.name
            landmarkAddressTextView.text = landmark.address
            landmarkRatingTextView.text =
                context.getString(R.string.recyclerView_rating_text, landmark.rating.toString())

            /**
             * Use Places API to further check landmarks' geographical coordinates.
             * It'll also be used to populate thumbnail images for each landmark.
             */

            // Define a search center based on the landmark's latitude and longitude
            val searchCenter = LatLng(landmark.latLng.latitude, landmark.latLng.longitude)

            // Create a Text Search Places API request object
            val searchByTextRequest = SearchByTextRequest.builder(landmark.name, placeFields)
                .setMaxResultCount(10)
                .setLocationBias(CircularBounds.newInstance(searchCenter, 1000.0))  // Search area based on searchCenter
                .build()

            /**
             * Call searchByText() to perform the search.
             * Define a response handler to process the returned List of Place objects;
             * Sets the thumbnail using the first (and thus closest) matching Place in the List of Place objects returned by the search response via PHOTO_METADATAS Place field.
             * Set an OnClickListener on the CardView which passes the aforementioned Place's LatLng and Name via an intent to the Google Maps fragment LandmarkMap activity,
             * and then navigate to that activity
             */
            placesClient.searchByText(searchByTextRequest)
                .addOnSuccessListener { response ->
                    val places: List<Place> = response.places

                    if (places.isNotEmpty()) {
                        val placeId = places[0].id ?: ""
                        fetchPlacePhotoViaId(placeId)

                        cardView.setOnClickListener {
                            val intent = Intent(context, LandmarkMap::class.java).apply {
                                putExtra(LANDMARKLATLNG_KEY, places[0].latLng)
                                putExtra(LANDMARKNAME_KEY, places[0].name)
                            }
                            context.startActivity(intent)
                        }
                    } else {
                        Log.w(TAG, "No places found for ${landmark.name}")
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.message}")
                    }
                }
        }

        // Get the Place's photo metadata via Place ID and use the open source Glide library to get the URI
        // and then set the photo to the CardView's ImageView in this RecyclerView
        private fun fetchPlacePhotoViaId(placeId: String) {
            progressBar.visibility = View.VISIBLE
            val fields = listOf(Place.Field.PHOTO_METADATAS)
            // Get a Place object using FetchPlaceRequest via Place ID, with the PHOTO_METADATAS field being returned in the Place object
            val placeRequest = FetchPlaceRequest.newInstance(placeId, fields)

            placesClient.fetchPlace(placeRequest)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
                    // Get the photo's metadata
                    val metadata = place.photoMetadatas
                    if (metadata.isNullOrEmpty()) {
                        Log.w(TAG, "No photo metadata found for place ID: $placeId")
                        progressBar.visibility = View.GONE
                        return@addOnSuccessListener
                    }
                    val photoMetadata = metadata[0]
                    fetchPhotoUri(photoMetadata)
                }
                .addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.message}")
                    }
                    progressBar.visibility = View.GONE
                }
        }

        /**
         * Retrieve the URI of a photo associated with a Place using the Google Places API.
         * Once the photo is retrieved, display it in an ImageView using the Glide library
         */
        private fun fetchPhotoUri(photoMetadata: PhotoMetadata) {
            // Create a FetchResolvedPhotoUriRequest using the PhotoMetadata object passed as an argument
            // Set image max dimensions
            val photoRequest = FetchResolvedPhotoUriRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build()

            // Request the photo URI of the photo corresponding to the PhotoMetadata
            placesClient.fetchResolvedPhotoUri(photoRequest)
                .addOnSuccessListener { response: FetchResolvedPhotoUriResponse ->
                    val uri = response.uri
                    // Load the image into the ImageView using the Glide library
                    val requestOptions = RequestOptions().override(Target.SIZE_ORIGINAL)  // Do not allow Glide to resize image
                    Glide.with(itemView.context).load(uri).apply(requestOptions).into(landmarkImageView)
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        Log.e(TAG, "Photo not found: ${exception.message}")
                    }
                    progressBar.visibility = View.GONE
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_landmark, parent, false)
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