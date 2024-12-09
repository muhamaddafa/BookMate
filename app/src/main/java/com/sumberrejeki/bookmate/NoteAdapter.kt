package com.sumberrejeki.bookmate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Notes
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(private val isHomeFragment: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECENT = 0
        private const val VIEW_TYPE_MY = 1
        private const val VIEW_TYPE_BOOK = 2
    }

    private val noteList = mutableListOf<Notes>()

    inner class RecentNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notesTitleTextView)
        val noteContent: TextView = itemView.findViewById(R.id.notesContentTextView)
    }

    inner class MyNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val date: TextView = itemView.findViewById(R.id.createdDate)
        val noteContent: TextView = itemView.findViewById(R.id.notesContentTextView)
        val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
    }

    inner class BookNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.createdDate)
        val noteContent: TextView = itemView.findViewById(R.id.notesContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RECENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recent_item_note, parent, false)
                RecentNoteViewHolder(view)
            }
            VIEW_TYPE_MY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.my_item_note, parent, false)
                MyNoteViewHolder(view)
            }
            VIEW_TYPE_BOOK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.cardview_notes, parent, false)
                BookNoteViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = noteList[position]
        when (holder) {
            is RecentNoteViewHolder -> {
                holder.titleTextView.text = "Recent Note"
                holder.noteContent.text = note.text
            }
            is MyNoteViewHolder -> {
                holder.noteContent.text = note.text
                holder.date.text = formatDate(note.created)
                fetchBookDetails(note.bookId, holder)
            }
            is BookNoteViewHolder -> {
                holder.noteContent.text = note.text
                holder.date.text = formatDate(note.created)
            }
        }
    }

    private fun formatDate(date: Date?): String {
        date ?: return "N/A"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    override fun getItemCount() = noteList.size

    override fun getItemViewType(position: Int): Int {
        if (isHomeFragment == "recent") {
            return VIEW_TYPE_RECENT
        } else if (isHomeFragment == "my") {
            return VIEW_TYPE_MY
        } else {
            return VIEW_TYPE_BOOK
        }
    }

    private fun fetchBookDetails(bookId: String, holder: MyNoteViewHolder) {
        val db = FirebaseFirestore.getInstance()
        db.collection("books").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val bookTitle = document.getString("title") ?: "Unknown Title"
                    val bookImageUrl = document.getString("imageUrl") ?: ""

                    holder.bookTitle.text = bookTitle
                    Glide.with(holder.itemView.context)
                        .load(bookImageUrl)
                        .centerCrop()
                        .into(holder.bookImage)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("NoteAdapter", "Error getting book details: ", exception)
            }
    }

    fun submitList(list: List<Notes>) {
        noteList.clear()
        noteList.addAll(list)
        notifyDataSetChanged()
    }
}