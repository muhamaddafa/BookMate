package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumberrejeki.bookmate.models.Books
import androidx.appcompat.widget.Toolbar

class ShelvesPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapterWithCheckBox
    private var displayBooks: ArrayList<Books> = arrayListOf()  // List buku yang ditampilkan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelves_page)

        val addBookButton: Button = findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            val intent = Intent(this, BookListShelves::class.java)
            startActivity(intent)
        }

        // Terima data dari Intent
        val selectedBooks = intent.getSerializableExtra("SELECTED_BOOKS") as? ArrayList<Books> ?: arrayListOf()
        displayBooks.addAll(selectedBooks)  // Tampilkan semua buku yang dipilih

        recyclerView = findViewById(R.id.recyclerViewBooks)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Inisialisasi Adapter
        bookAdapter = BookAdapterWithCheckBox(
            onItemClick = { book ->
                Toast.makeText(this, "Clicked: ${book.title}", Toast.LENGTH_SHORT).show()
            },
            onCheckedChange = { book, isChecked ->  // Tambahkan parameter onCheckedChange
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
    }

    private fun deleteSelectedBooks() {
        val selectedBooks = bookAdapter.getSelectedBooks()  // Ambil buku yang dicentang dari adapter
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected for deletion", Toast.LENGTH_SHORT).show()
            return
        }

        // Hapus hanya buku yang dicentang dari displayBooks
        displayBooks.removeAll { book -> selectedBooks.contains(book) }

        // Update RecyclerView
        bookAdapter.submitList(displayBooks)
        Toast.makeText(this, "Selected books deleted", Toast.LENGTH_SHORT).show()
    }
}
