package com.sumberrejeki.bookmate

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditBookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchBooks() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("books").whereEqualTo("userId", userId).limit(4)

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