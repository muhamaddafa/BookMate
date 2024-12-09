package com.sumberrejeki.bookmate

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ShelvesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var shelfTitleTextView: TextView
    private lateinit var shelfImageView: ImageView
    private lateinit var shelfDescriptionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shelves)

        // Initialize Firestore and Storage
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set up the toolbar with arrow back
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)

        // Bind views
        shelfTitleTextView = findViewById(R.id.shelf_title_edit_text) // Update ID for TextView
        shelfImageView = findViewById(R.id.shelf_image_view)
        shelfDescriptionTextView = findViewById(R.id.shelf_description_edit_text)

        // Retrieve shelfId passed from the adapter
        val shelfId = intent.getStringExtra("shelfId")
        Log.d("ShelvesActivity", "Shelf ID: $shelfId") // Tambahkan di sini
        if (shelfId != null) {
            loadShelfData(shelfId) // Load data using shelfId
        } else {
            Log.e("ShelvesActivity", "No Shelf ID received!")
        }
    }

    private fun loadShelfData(shelfId: String) {
        firestore.collection("shelves").document(shelfId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"
                    val imageUrl = document.getString("imageUrl")

                    // Debug Log
                    Log.d("ShelvesActivity", "Shelf ID: $shelfId")
                    Log.d("ShelvesActivity", "Image URL: $imageUrl")

                    // Update UI
                    shelfTitleTextView.text = title
                    shelfDescriptionTextView.text = description

                    if (!imageUrl.isNullOrEmpty()) {
                        Log.d("ShelvesActivity", "Loading image from URL: $imageUrl")
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.image_placeholder)
                            .skipMemoryCache(true)
                            .into(shelfImageView)
                    } else {
                        Log.e("ShelvesActivity", "Image URL is empty")
                        shelfImageView.setImageResource(R.drawable.image_placeholder)
                    }
                } else {
                    Log.e("ShelvesActivity", "No such document!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShelvesActivity", "Error fetching document", e)
            }
    }



    // Handle arrow back button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Ends the current activity and goes back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
