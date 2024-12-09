package com.sumberrejeki.bookmate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sumberrejeki.bookmate.models.Books
import java.util.*

class AddBookActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImageUri: Uri
    private lateinit var bookImage: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        bookImage = findViewById(R.id.bookImage)

        val cameraButton = findViewById<ImageView>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            selectImage()
        }

        val addBookButton = findViewById<Button>(R.id.addBookButton)
        addBookButton.setOnClickListener {
            uploadImageAndAddBookToFirestore()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    // Fungsi untuk memilih gambar dari galeri
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Mendapatkan hasil gambar yang dipilih
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data ?: return
            bookImage.setImageURI(selectedImageUri)  // Menampilkan gambar di ImageView
        }
    }

    // Upload gambar ke Firebase Storage dan tambah buku ke Firestore
    private fun uploadImageAndAddBookToFirestore() {
        if (::selectedImageUri.isInitialized) {
            val storageRef = storage.reference.child("book_images/${UUID.randomUUID()}.jpg")

            // Upload gambar ke Firebase Storage
            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        addBookToFirestore(imageUrl)  // Setelah berhasil upload gambar, tambah buku ke Firestore
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengupload gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika tidak ada gambar yang dipilih
            addBookToFirestore(null)
        }
    }

    // Tambah buku ke Firestore dengan URL gambar
    private fun addBookToFirestore(imageUrl: String?) {
        val userId = auth.currentUser?.uid ?: return

        // Get data from the form
        val title = findViewById<EditText>(R.id.bookTitle).text.toString()
        val author = findViewById<EditText>(R.id.bookAuthor).text.toString()
        val publisher = findViewById<EditText>(R.id.bookPublisher).text.toString()
        val pages = findViewById<EditText>(R.id.bookPages).text.toString().toIntOrNull() ?: 0
        val description = findViewById<EditText>(R.id.bookDescription).text.toString()

        // Generate a new document ID
        val newBookRef = db.collection("books").document() // This generates a new document reference with a unique ID
        val id = newBookRef.id // Get the generated document ID

        // Create a book object with User ID and image URL
        val book = Books(id, title, author, publisher, pages, description, userId).apply {
            this.imageUrl = imageUrl // Add the image URL to the Book model
        }

        // Save the book to Firestore
        newBookRef.set(book) // Use set() to save the book object to the generated document reference
            .addOnSuccessListener {
                Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show()
                finish()  // Go back to MainActivity after successfully adding the book
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
