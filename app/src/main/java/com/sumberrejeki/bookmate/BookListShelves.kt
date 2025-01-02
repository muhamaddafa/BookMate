package com.sumberrejeki.bookmate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Books
import androidx.appcompat.widget.Toolbar

class BookListShelves : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapterWithCheckBox
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list_shelves)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerViewBooks)

        // Initialize adapter with callback
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        bookAdapter = BookAdapterWithCheckBox(
            onItemClick = { book ->
            },
            onCheckedChange = { book, isChecked ->
            }
        )
        recyclerView.adapter = bookAdapter

        // Initialize Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Get shelfId from Intent
        val shelfId = intent.getStringExtra("shelfId")
        Log.d("BookListShelves", "Received Shelf ID: $shelfId")

        // Fetch books excluding those already in the shelf
        if (shelfId != null) {
            fetchBooksNotInShelf(auth.currentUser?.uid ?: "", shelfId)
        } else {
            Toast.makeText(this, "Error: Shelf ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Button to add selected books
        val addButton: Button = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            addSelectedBooksToShelves(shelfId)
        }
    }

    private fun fetchBooksNotInShelf(userId: String, shelfId: String) {
        // First, get the existing books in the shelf
        db.collection("shelves")
            .document(shelfId)
            .get()
            .addOnSuccessListener { shelfDoc ->
                // Get the array of book IDs from the shelf
                val existingBookIds = shelfDoc.get("books") as? List<String> ?: listOf()

                // Then fetch all user's books that are not in the shelf
                db.collection("books")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        val bookList = documents.mapNotNull { document ->
                            // Only include books that are not in the shelf
                            if (!existingBookIds.contains(document.id)) {
                                document.toObject(Books::class.java).apply {
                                    this.id = document.id
                                }
                            } else null
                        }
                        bookAdapter.submitList(bookList)
                        Log.d("BookListShelves", "Fetched Books (not in shelf): ${bookList.map { it.id }}")
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Error fetching books: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error fetching shelf data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addSelectedBooksToShelves(shelfId: String?) {
        val selectedBooks = bookAdapter.getSelectedBooks()
        val selectedBooksWithIds = selectedBooks.filter { it.id != null }
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected", Toast.LENGTH_SHORT).show()
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_BOOKS", ArrayList(selectedBooksWithIds))
            resultIntent.putExtra("shelfId", shelfId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
