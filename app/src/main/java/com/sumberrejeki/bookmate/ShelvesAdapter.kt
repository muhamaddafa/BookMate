package com.sumberrejeki.bookmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumberrejeki.bookmate.models.Shelf

class ShelvesAdapter(private val shelvesList: List<Shelf>) : RecyclerView.Adapter<ShelvesAdapter.ShelfViewHolder>() {

    class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.bookImageView1)
        val descriptionView: TextView = itemView.findViewById(R.id.bookDescriptionTextView1)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_shelf, parent, false)
        return ShelfViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        val shelf = shelvesList[position]

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(shelf.imageUrl)
            .into(holder.imageView)

        // Set data
        holder.bookTitleTextView.text = shelf.title
        holder.descriptionView.text = shelf.description
    }

    override fun getItemCount(): Int {
        return shelvesList.size
    }
}
