package com.sumberrejeki.bookmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumberrejeki.bookmate.models.Books

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val bookList = mutableListOf<Books>()

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val pageCountTextView: TextView = itemView.findViewById(R.id.pageCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.author
        holder.pageCountTextView.text = "P.0 / ${book.pages}"

        // Load image using your preferred image loading library
        // Example with Glide:
        Glide.with(holder.coverImageView)
            .load(book.imageUrl)
            .into(holder.coverImageView)
    }

    override fun getItemCount() = bookList.size

    fun submitList(list: List<Books>) {
        bookList.clear()
        bookList.addAll(list)
        notifyDataSetChanged()
    }
}