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

class NoteAdapter(private val isHomeFragment: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_RECENT = 0
        private const val VIEW_TYPE_MY = 1
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
        }
    }

    private fun formatDate(date: Date?): String {
        date ?: return "N/A"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    override fun getItemCount() = noteList.size

    override fun getItemViewType(position: Int): Int {
        return if (isHomeFragment) VIEW_TYPE_RECENT else VIEW_TYPE_MY
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