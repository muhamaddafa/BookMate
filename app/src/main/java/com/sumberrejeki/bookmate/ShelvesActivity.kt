package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Books

class ShelvesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var shelfTitleTextView: TextView
    private lateinit var shelfImageView: ImageView
    private lateinit var shelfDescriptionTextView: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shelves)
        recyclerView = findViewById(R.id.booksRecyclerView)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns
        bookAdapter = BookAdapter { book ->
            navigateToBookDetail(book)
        }
        recyclerView.adapter = bookAdapter

        // Set up the toolbar with arrow back
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_back)

        // Bind views
        shelfTitleTextView = findViewById(R.id.shelf_title_edit_text)
        shelfImageView = findViewById(R.id.shelf_image_view)
        shelfDescriptionTextView = findViewById(R.id.shelf_description_edit_text)

        // Retrieve shelfId passed from the adapter
        val shelfId = intent.getStringExtra("shelfId")
        Log.d("ShelvesActivity", "Shelf ID: $shelfId")

        if (shelfId != null) {
            loadShelfData(shelfId)
        } else {
            Log.e("ShelvesActivity", "No Shelf ID received!")
        }
    }

    private fun loadShelfData(shelfId: String) {
        firestore.collection("shelves").document(shelfId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Extract shelf details
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"
                    val imageUrl = document.getString("imageUrl") // Image URL saved in Firestore
                    val bookIds = document.get("books") as? List<String> ?: emptyList()

                    // Update UI with shelf details
                    shelfTitleTextView.text = title
                    shelfDescriptionTextView.text = description

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .into(shelfImageView)
                    } else {
                        shelfImageView.setImageResource(R.drawable.image_placeholder)
                    }

                    // Fetch books associated with this shelf
                    if (bookIds.isNotEmpty()) {
                        fetchBooks(bookIds)
                    } else {
                        Log.d("ShelvesActivity", "No books associated with this shelf.")
                    }
                } else {
                    Log.e("ShelvesActivity", "No document found for shelfId: $shelfId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShelvesActivity", "Error fetching shelf data", e)
            }
    }

    private fun fetchBooks(bookIds: List<String>) {
        firestore.collection("books")
            .whereIn(FieldPath.documentId(), bookIds)
            .get()
            .addOnSuccessListener { result ->
                val books = result.map { document ->
                    document.toObject(Books::class.java) // Assuming `Books` is your data model
                }
                bookAdapter.submitList(books)
            }
            .addOnFailureListener { e ->
                Log.e("ShelvesActivity", "Error fetching books", e)
            }
    }

    private fun navigateToBookDetail(book: Books) {
        val intent = Intent(this, BookScreenActivity::class.java)
        intent.putExtra("BOOK_TITLE", book.title)
        intent.putExtra("BOOK_ID", book.id)
        startActivity(intent)
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
