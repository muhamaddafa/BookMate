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

        // Terima shelfId dari Intent
        val shelfId = intent.getStringExtra("shelfId")
        Log.d("BookListShelves", "Received Shelf ID: $shelfId")

        // Ambil data buku dari Firestore
        fetchBooks(auth.currentUser?.uid ?: "")

        // Tombol untuk menambahkan buku yang dipilih
        val addButton: Button = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            addSelectedBooksToShelves(shelfId)
        }
    }

    private fun fetchBooks(userId: String) {
        db.collection("books")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val bookList = documents.map { document ->
                    document.toObject(Books::class.java).apply {
                        this.id = document.id  // Set Firestore ID ke dalam objek Books
                    }
                }
                bookAdapter.submitList(bookList)
                Log.d("BookListShelves", "Fetched Books: ${bookList.map { it.id }}")  // Log ID buku
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching books: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addSelectedBooksToShelves(shelfId: String?) {
        val selectedBooks = bookAdapter.getSelectedBooks()
        val selectedBooksWithIds = selectedBooks.filter { it.id != null }  // Filter buku tanpa ID
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected", Toast.LENGTH_SHORT).show()
        } else {
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_BOOKS", ArrayList(selectedBooksWithIds))
            resultIntent.putExtra("shelfId", shelfId)  // Kirim kembali shelfId
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
