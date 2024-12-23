package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
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

        // Inisialisasi adapter dengan callback
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        bookAdapter = BookAdapterWithCheckBox(
            onItemClick = { book ->
                Toast.makeText(this, "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
            },
            onCheckedChange = { book, isChecked ->
                if (isChecked) {
                    Toast.makeText(this, "${book.title} selected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "${book.title} unselected", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = bookAdapter

        // Inisialisasi Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Ambil data buku dari Firestore
        fetchBooks(auth.currentUser?.uid ?: "")

        // Tombol untuk menambahkan buku yang dipilih
        val addButton: Button = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            addSelectedBooksToShelves()
        }
    }

    private fun fetchBooks(userId: String) {
        db.collection("books")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val bookList = documents.map { document ->
                    document.toObject(Books::class.java)
                }
                bookAdapter.submitList(bookList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching books: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addSelectedBooksToShelves() {
        val selectedBooks = bookAdapter.getSelectedBooks()
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, ShelvesPageActivity::class.java)
            intent.putExtra("SELECTED_BOOKS", ArrayList(selectedBooks))
            startActivity(intent)
        }
    }
}
