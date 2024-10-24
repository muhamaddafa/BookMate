package com.sumberrejeki.bookmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sumberrejeki.bookmate.models.Notes

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val noteList = mutableListOf<Notes>()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notesTitleTextView)
        val contentTextView: TextView = itemView.findViewById(R.id.notesContentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.titleTextView.text = "Note"
        holder.contentTextView.text = note.text
    }

    override fun getItemCount() = noteList.size

    fun submitList(list: List<Notes>) {
        noteList.clear()
        noteList.addAll(list)
        notifyDataSetChanged()
    }
}