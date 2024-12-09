package com.sumberrejeki.bookmate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sumberrejeki.bookmate.models.Books
import java.util.UUID

class EditBookActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImageUri: Uri
    private lateinit var bookImage: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var bookId: String? = null // To hold the ID of the book being edited

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_book)

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Fetch existing book data
        fetchBookData()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bookImage = findViewById(R.id.bookImage)

        val cameraButton = findViewById<ImageView>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            selectImage()
        }

        val saveBookButton = findViewById<Button>(R.id.saveBookButton)
        saveBookButton.setOnClickListener {
            updateBookInFirestore()
        }
    }

    // Fetch existing book data using the book ID
    private fun fetchBookData() {
        // Retrieve the book ID from the intent
        bookId = intent.getStringExtra("BOOK_ID") // Ensure you are getting the correct ID

        bookId?.let { id ->
            val docRef = db.collection("books").document(id) // Use document() to get the specific book

            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val title = document.getString("title") ?: "No Title"
                    val author = document.getString("author") ?: "Unknown Author"
                    val publisher = document.getString("publisher") ?: "Unknown Publisher"
                    val pages = document.getLong("pages") ?: 0
                    val description = document.getString("description") ?: "No Description"
                    val imageUrl = document.getString("imageUrl")

                    // Populate the fields with existing data
                    findViewById<EditText>(R.id.bookTitle).setText(title)
                    findViewById<EditText>(R.id.bookAuthor).setText(author)
                    findViewById<EditText>(R.id.bookPublisher).setText(publisher)
                    findViewById<EditText>(R.id.bookPages).setText(pages.toString())
                    findViewById<EditText>(R.id.bookDescription).setText(description)

                    // Load existing image using Glide
                    imageUrl?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.drawable.image_placeholder)
                            .into(bookImage)
                    }
                } else {
                    Log.d("EditBookActivity", "No book found with ID: $id")
                }
            }.addOnFailureListener { exception ->
                Log.d("EditBookActivity", "Failed to fetch book: ", exception)
            }
        } ?: run {
            Log.d("EditBookActivity", "Book ID is null")
        }
    }

    // Function to select an image from the gallery
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the result of the image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data ?: return
            bookImage.setImageURI(selectedImageUri)  // Display the selected image
        }
    }

    // Update the book in Firestore
    private fun updateBookInFirestore() {
        val userId = auth.currentUser?.uid ?: return

        // Get data from the form
        val title = findViewById<EditText>(R.id.bookTitle).text.toString()
        val author = findViewById<EditText>(R.id.bookAuthor).text.toString()
        val publisher = findViewById<EditText>(R.id.bookPublisher).text.toString()
        val pages = findViewById<EditText>(R.id.bookPages).text.toString().toIntOrNull() ?: 0
        val description = findViewById<EditText>(R.id.bookDescription).text.toString()

        // Create a book object
        val book = Books(bookId, title, author, publisher, pages, description, userId)

        // Update the book document in Firestore
        bookId?.let {
            Log.d("EditBookActivity", "Updating book with ID: $it")
            Log.d("EditBookActivity", "Book data: $book")
            db.collection("books").document(it)
                .set(book)
                .addOnSuccessListener {
                    Toast.makeText(this, "Book Updated!", Toast.LENGTH_SHORT).show()
                    // Create an Intent to start the BookDetailActivity
                    val intent = Intent(this, BookScreenActivity::class.java)
                    intent.putExtra("BOOK_TITLE", book.title)
                    // book id
                    intent.putExtra("BOOK_ID", book.id)
                    // Add any other data you want to pass
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to Update Book: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
