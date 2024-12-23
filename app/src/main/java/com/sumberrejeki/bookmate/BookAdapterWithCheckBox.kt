package com.sumberrejeki.bookmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sumberrejeki.bookmate.models.Books

class BookAdapterWithCheckBox(
    private val onItemClick: (Books) -> Unit,
    private val onCheckedChange: (Books, Boolean) -> Unit
) : RecyclerView.Adapter<BookAdapterWithCheckBox.BookViewHolder>() {

    private val bookList = mutableListOf<Books>()
    private val selectedBooks = mutableSetOf<Books>()  // Menyimpan buku yang dipilih

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.bookCoverImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.bookTitleTextView)
        val authorTextView: TextView = itemView.findViewById(R.id.bookAuthorTextView)
        val pageCountTextView: TextView = itemView.findViewById(R.id.pageCountTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.bookCheckBox)

        init {
            // Handle klik pada seluruh item (bukan hanya checkbox)
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(bookList[position])
                }
            }
        }

        fun bind(book: Books) {
            titleTextView.text = book.title
            authorTextView.text = book.author
            pageCountTextView.text = "Pages: ${book.pages ?: 0}"

            // Load image menggunakan Glide
            Glide.with(coverImageView.context)
                .load(book.imageUrl)
                .into(coverImageView)

            // Cek apakah buku ini sebelumnya dipilih
            checkBox.isChecked = selectedBooks.contains(book)

            // Tangani perubahan CheckBox
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedBooks.add(book)
                } else {
                    selectedBooks.remove(book)
                }
                onCheckedChange(book, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_booklist_checkbox, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.bind(book)
    }

    override fun getItemCount() = bookList.size

    fun submitList(list: List<Books>) {
        bookList.clear()
        bookList.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelectedBooks(): List<Books> {
            return selectedBooks.toList()
    }
}
