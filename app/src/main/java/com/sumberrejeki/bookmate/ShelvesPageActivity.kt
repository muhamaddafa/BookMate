package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Books
import androidx.appcompat.widget.Toolbar

class ShelvesPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapterWithCheckBox
    private lateinit var firestore: FirebaseFirestore

    // UI Elements dari layout baru
    private lateinit var shelfTitle: EditText
    private lateinit var shelfDescription: EditText
    private lateinit var shelvesImage: ImageView
    private lateinit var progressBar: ProgressBar

    private var displayBooks: ArrayList<Books> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelves_page)

        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi UI sesuai dengan layout baru
        shelfTitle = findViewById(R.id.shelf_title)
        shelfDescription = findViewById(R.id.shelf_description)
        shelvesImage = findViewById(R.id.shelves_image)
        progressBar = findViewById(R.id.progressBar)

        val addBookButton: Button = findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            val intent = Intent(this, BookListShelves::class.java)
            startActivity(intent)
        }

        // Terima data dari Intent
        val selectedBooks = intent.getSerializableExtra("SELECTED_BOOKS") as? ArrayList<Books> ?: arrayListOf()
        displayBooks.addAll(selectedBooks)

        recyclerView = findViewById(R.id.recyclerViewBooks)
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
        bookAdapter.submitList(displayBooks)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val deleteButton: Button = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            deleteSelectedBooks()
        }

        // Ambil data Firestore berdasarkan Shelf ID
        val shelfId = intent.getStringExtra("SHELF_ID") ?: ""
        if (shelfId.isNotEmpty()) {
            fetchShelfDataFromFirestore(shelfId)
        }
    }

    // Fungsi untuk mengambil data dari Firestore
    private fun fetchShelfDataFromFirestore(shelfId: String) {
        progressBar.visibility = ProgressBar.VISIBLE

        firestore.collection("shelves").document(shelfId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val description = document.getString("description") ?: "No Description"
                    val imageUrl = document.getString("imageUrl") ?: ""

                    // Update UI dengan data dari Firestore
                    shelfTitle.setText(title)
                    shelfDescription.setText(description)

                    // Load image dengan Glide
                    Glide.with(this)
                        .load(imageUrl)
                        .into(shelvesImage)
                } else {
                    Toast.makeText(this, "Shelf not found", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = ProgressBar.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSelectedBooks() {
        val selectedBooks = bookAdapter.getSelectedBooks()
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected for deletion", Toast.LENGTH_SHORT).show()
            return
        }

        displayBooks.removeAll { book -> selectedBooks.contains(book) }
        bookAdapter.submitList(displayBooks)
        Toast.makeText(this, "Selected books deleted", Toast.LENGTH_SHORT).show()
    }
}
