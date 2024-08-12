package com.comp304.lab4.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.comp304.lab4.R
import com.comp304.lab4.data.Landmark
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? {
        // Get tag
        val landmark = marker.tag as? Landmark ?: return null

        // Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = landmark.name
        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = landmark.address
        view.findViewById<TextView>(
            R.id.text_view_rating
        ).text = context.getString(R.string.recyclerView_rating_text, landmark.rating.toString())

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}