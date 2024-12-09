package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ShelvesPageActivity : AppCompatActivity() {

    private lateinit var addBook: Button
    private lateinit var deleteShelvesButton: Button
    private lateinit var saveShelvesButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: TextView

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var shelfId: String = ""  // Simpan ID rak yang sedang dipilih (dari Firestore)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelves_page)

        // Inisialisasi Firestore dan FirebaseAuth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Inisialisasi UI components
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        addBook = findViewById(R.id.add_book_button)
        deleteShelvesButton = findViewById(R.id.delete_shelves_button)
        saveShelvesButton = findViewById(R.id.save_shelves_button)

        titleEditText = findViewById(R.id.shelf_title_edit_text)
        descriptionEditText = findViewById(R.id.shelf_description_edit_text)

        // Ambil ID rak dari intent
        shelfId = intent.getStringExtra("shelf_id") ?: ""

        // Panggil fungsi untuk mengambil data rak
        fetchShelvesData()

        // Add Book button
        addBook.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            intent.putExtra("shelf_id", shelfId)  // Kirim ID rak ke activity AddBookActivity
            startActivity(intent)
        }

        // Delete Shelves button
        deleteShelvesButton.setOnClickListener {
            deleteShelf()
        }

        // Save Shelves button
        saveShelvesButton.setOnClickListener {
            saveShelf()
        }
    }

    // Fetch data rak dari Firestore
    private fun fetchShelvesData() {
        if (shelfId.isNotEmpty()) {
            db.collection("shelves").document(shelfId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val title = document.getString("title")
                        val description = document.getString("description")

                        // Menampilkan title dan description di EditText
                        titleEditText.setText(title)
                        descriptionEditText.text = description
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to fetch shelf data: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Save changes to shelf
    private fun saveShelf() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val shelfData = hashMapOf(
            "title" to title,
            "description" to description,
            "user_id" to auth.currentUser?.uid // Tambahkan ID user untuk data pengguna
        )

        // Update data rak di Firestore
        db.collection("shelves").document(shelfId)
            .set(shelfData)
            .addOnSuccessListener {
                Toast.makeText(this, "Shelf saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving shelf: $e", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete shelf from Firestore
    private fun deleteShelf() {
        db.collection("shelves").document(shelfId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Shelf deleted successfully", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke layar sebelumnya setelah rak dihapus
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting shelf: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
