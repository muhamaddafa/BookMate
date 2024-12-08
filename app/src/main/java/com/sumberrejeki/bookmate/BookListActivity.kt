package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Books

class BookListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerViewBooks)

        // Set up RecyclerView with GridLayoutManager
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns
        bookAdapter = BookAdapter { book ->
            navigateToBookDetail(book)
        }
        recyclerView.adapter = bookAdapter

        // Initialize toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        fetchBooks(auth.currentUser?.uid ?: "")
    }

    private fun fetchBooks(userId: String) {
        db.collection("books")
            .whereEqualTo("userId", userId) // Filter books by user ID
            .get()
            .addOnSuccessListener { documents ->
                val bookList = documents.map { document ->
                    document.toObject(Books::class.java)
                }
                bookAdapter.submitList(bookList)
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    private fun navigateToBookDetail(book: Books) {
        // Create an Intent to start the BookDetailActivity
        val intent = Intent(this, BookScreenActivity::class.java)
        intent.putExtra("BOOK_TITLE", book.title)
        // Add any other data you want to pass
        startActivity(intent)
    }
}
