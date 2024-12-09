package com.sumberrejeki.bookmate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumberrejeki.bookmate.models.Shelf

class ShelvesAdapter(private val shelvesList: List<Shelf>) : RecyclerView.Adapter<ShelvesAdapter.ShelfViewHolder>() {

    class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.bookImageView1)
        val descriptionView: TextView = itemView.findViewById(R.id.bookDescriptionTextView1)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView1)
        val editButtonLayout: LinearLayout = itemView.findViewById(R.id.editButtonLayout)
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

        // Set click listener for the entire CardView
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ShelvesActivity::class.java)
            intent.putExtra("shelfId", shelf.id) // Pass additional data if needed
            context.startActivity(intent)
        }

        // Set click listener for editButtonLayout
        holder.editButtonLayout.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ShelvesPageActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return shelvesList.size
    }
}