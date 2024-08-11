package com.comp304.lab4.adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
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
import com.comp304.lab4.LandmarkList
import com.comp304.lab4.LandmarkMap
import com.comp304.lab4.R
import com.comp304.lab4.data.Landmark
import com.comp304.lab4.data.LandmarkReader
import com.comp304.lab4.util.Constants
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

    private val landmarks: List<Landmark> by lazy {
        LandmarkReader(context).read(landmarkType)
    }

    private val placesClient: PlacesClient = Places.createClient(context)

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardLandmark)
        private val landmarkNameTextView: TextView = itemView.findViewById(R.id.textViewName)
        private val landmarkAddressTextView: TextView = itemView.findViewById(R.id.textViewAddress)
        private val landmarkRatingTextView: TextView = itemView.findViewById(R.id.textViewRating)
        private val landmarkImageView: ImageView = itemView.findViewById(R.id.imageViewLandmarkPreview)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        private val placeFields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        fun bind(landmark: Landmark) {
            landmarkNameTextView.text = landmark.name
            landmarkAddressTextView.text = landmark.address
            landmarkRatingTextView.text =
                context.getString(R.string.recyclerView_rating_text, landmark.rating.toString())



            val searchCenter = LatLng(landmark.latLng.latitude, landmark.latLng.longitude)

            val searchByTextRequest = SearchByTextRequest.builder(landmark.name, placeFields)
                .setMaxResultCount(10)
                .setLocationBias(CircularBounds.newInstance(searchCenter, 1000.0))
                .build()

            placesClient.searchByText(searchByTextRequest)
                .addOnSuccessListener { response ->
                    val places: List<Place> = response.places

                    if (places.isNotEmpty()) {
                        val placeId = places[0].id ?: ""
                        fetchPlaceDetailsAndPhoto(placeId)

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

        private fun fetchPlaceDetailsAndPhoto(placeId: String) {
            progressBar.visibility = View.VISIBLE
            val fields = listOf(Place.Field.PHOTO_METADATAS)
            val placeRequest = FetchPlaceRequest.newInstance(placeId, fields)

            placesClient.fetchPlace(placeRequest)
                .addOnSuccessListener { response: FetchPlaceResponse ->
                    val place = response.place
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

        private fun fetchPhotoUri(photoMetadata: PhotoMetadata) {
            val photoRequest = FetchResolvedPhotoUriRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build()

            placesClient.fetchResolvedPhotoUri(photoRequest)
                .addOnSuccessListener { response: FetchResolvedPhotoUriResponse ->
                    val uri = response.uri
                    val requestOptions = RequestOptions().override(Target.SIZE_ORIGINAL)
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