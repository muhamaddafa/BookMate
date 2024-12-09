package com.sumberrejeki.bookmate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        auth.currentUser?.let{ user ->
            fetchUserData()
            fetchNotes(user.uid)
        }
    }

    private fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val photoUrl = document.getString("photoUrl") // Tambahkan ini

                    // Memuat gambar menggunakan Glide
                    val imageView = view?.findViewById<ImageView>(R.id.avatarImageView)
                    imageView?.let {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.image_placeholder) // Gambar placeholder saat loading
                            .error(R.drawable.image_placeholder) // Gambar error jika gagal memuat
                            .into(it) // 'it' merujuk pada imageView yang non-null
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
                noteAdapter.submitList(notesList)
            }
            .addOnFailureListener { exception ->
                Log.w("NotesFragment", "Error getting notes: ", exception)
            }
    }
}