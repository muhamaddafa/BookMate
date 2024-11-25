package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Books

class BookScreenActivity : AppCompatActivity() {

    private lateinit var editBookButton: ImageView
    private lateinit var addNoteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_screen)

        // Initialize toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Initialize buttons
        editBookButton = findViewById(R.id.editBookButton)
        editBookButton.setOnClickListener {
            startActivity(Intent(this, EditBookActivity::class.java))
        }

        addNoteButton = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            startActivity(Intent(this, AddNotesActivity::class.java))
        }

        // Fetch books data
        fetchBooks()
    }

    private fun fetchBooks() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val title = intent.getStringExtra("BOOK_TITLE") ?: "No Title"
            val docRef = db.collection("books").whereEqualTo("title", title)

            docRef.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val title = document.getString("title") ?: "No Title"
                        val author = document.getString("author") ?: "Unknown Author"
                        val publisher = document.getString("publisher") ?: "Unknown Publisher"
                        val totalPages = document.getLong("pages") ?: 0
                        val imageUrl = document.getString("imageUrl")
                        val desc = document.getString("description") ?: "No Description"

                        // Update UI with book data
                        findViewById<TextView>(R.id.titleTextView)?.text = title
                        findViewById<TextView>(R.id.authorTextView)?.text = author
                        findViewById<TextView>(R.id.publisherTextView)?.text = publisher
                        findViewById<TextView>(R.id.totalPagesTextView)?.text = totalPages.toString()
                        findViewById<TextView>(R.id.descriptionTextView)?.text = desc

                        // Load image using Glide
                        val imageView = findViewById<ImageView>(R.id.bookImageView)
                        imageUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.image_placeholder)
                                .error(R.drawable.image_placeholder)
                                .into(imageView)
                        }
                    }
                } else {
                    Log.d("BookScreenActivity", "No books found for user: $userId")
                }
            }.addOnFailureListener { exception ->
                Log.e("BookScreenActivity", "Error getting documents: ", exception)
            }
        } ?: run {
            Log.w("BookScreenActivity", "User is not logged in.")
        }
    }
}
