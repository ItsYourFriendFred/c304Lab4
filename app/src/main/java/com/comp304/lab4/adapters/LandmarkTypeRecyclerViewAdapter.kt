package com.comp304.lab4.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.comp304.lab4.LandmarkList
import com.comp304.lab4.R
import com.comp304.lab4.util.Constants.LANDMARKTYPE_KEY

internal class LandmarkTypeRecyclerViewAdapter(
    private val context: Context
) : RecyclerView.Adapter<LandmarkTypeRecyclerViewAdapter.MyViewHolder>() {

    private var landmarkTypes: List<String> = context.resources.getStringArray(R.array.arrayLandmarkType).toList()

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val landmarkTypeTextView: TextView = itemView.findViewById(R.id.textViewLandmarkType)
        private val cardView: CardView = itemView.findViewById(R.id.cardLandmarkType)

        fun bind(landmarkType: String) {
            landmarkTypeTextView.text = landmarkType

            cardView.setOnClickListener {
                val intent = Intent(context, LandmarkList::class.java).apply {
                    putExtra(LANDMARKTYPE_KEY, landmarkType)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_landmark_types, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return landmarkTypes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = landmarkTypes[position]
        holder.bind(currentItem)
    }
}