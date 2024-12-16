package com.sumberrejeki.bookmate

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Notes

class NotesFragment : BaseAuthFragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var noteAdapter: NoteAdapter
    private var originalNotesList: List<Notes> = emptyList() // Store the original notes list

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        noteAdapter = NoteAdapter(isHomeFragment = "my")
        view.findViewById<RecyclerView>(R.id.myNotesRecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = noteAdapter
        }

        // Set up the search EditText
        val searchEditText: EditText = view.findViewById(R.id.searchNotes) // Make sure to set the correct ID
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove any pending posts of the searchRunnable
                searchRunnable?.let { handler.removeCallbacks(it) }

                // Create a new Runnable to perform the search
                searchRunnable = Runnable {
                    filterNotes(s.toString())
                }

                // Post the Runnable with a delay of 1.5 seconds
                handler.postDelayed(searchRunnable!!, 1500)
            }
        })

        auth.currentUser?.let { user ->
            fetchUserData()
            fetchNotes(user.uid)
        }
    }

    private fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val photoUrl = document.getString("photoUrl")

                    // Load image using Glide
                    val imageView = view?.findViewById<ImageView>(R.id.avatarImageView)
                    imageView?.let {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .into(it)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.d("NotesFragment", "get failed with ", exception)
            }
        }
    }

    private fun fetchNotes(userId: String) {
        db.collection("notes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val notesList = documents.mapNotNull { it.toObject(Notes::class.java) }
                originalNotesList = notesList // Store the original list
                noteAdapter.submitList(notesList)
            }
            .addOnFailureListener { exception ->
                Log.w("NotesFragment", "Error getting notes: ", exception)
            }
    }

    private fun filterNotes(query: String) {
        val filteredList = originalNotesList.filter { note ->
            note.text?.contains(query, ignoreCase = true) == true
        }
        noteAdapter.submitList(filteredList)
    }
}
