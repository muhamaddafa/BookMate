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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sumberrejeki.bookmate.models.Books
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.UUID

class ShelvesPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapterWithCheckBox
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 2
    private var shelfId: String? = null
    private var existingImageUrl: String? = null
    private lateinit var selectedImageUri: Uri
    private lateinit var shelvesImage: ImageView
    private lateinit var shelfTitle: TextInputEditText
    private lateinit var shelfDescription: TextInputEditText
    private var displayBooks: ArrayList<Books> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelves_page)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        initializeViews()

        // Get shelfId from savedInstanceState or intent
        shelfId = savedInstanceState?.getString("shelfId") ?: intent.getStringExtra("shelfId")
        Log.d("ShelvesPageActivity", "Shelf ID: $shelfId")

        // Fetch shelf data if shelfId exists
        if (shelfId != null) {
            fetchShelfDataFromFirestore(shelfId!!)
        } else {
            Log.e("ShelvesPageActivity", "No Shelf ID received!")
        }

        // Setup RecyclerView
        setupRecyclerView()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        shelfTitle = findViewById(R.id.shelf_title)
        shelfDescription = findViewById(R.id.shelf_description)
        shelvesImage = findViewById(R.id.shelves_image)
        recyclerView = findViewById(R.id.recyclerViewBooks)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        bookAdapter = BookAdapterWithCheckBox(
            onItemClick = { book -> },
            onCheckedChange = { book, isChecked ->
                if (isChecked) {
                    displayBooks.add(book)
                } else {
                    displayBooks.remove(book)
                }
            }
        )
        recyclerView.adapter = bookAdapter
        bookAdapter.submitList(displayBooks)
    }

    private fun setupClickListeners() {
        // Add Book Button
        findViewById<Button>(R.id.add_book_button).setOnClickListener {
            val intent = Intent(this, BookListShelves::class.java)
            intent.putExtra("shelfId", shelfId)
            startActivityForResult(intent, 1)
        }

        // Delete Button
        findViewById<Button>(R.id.delete_button).setOnClickListener {
            deleteSelectedBooks()
        }

        // Save Shelf Button
        findViewById<Button>(R.id.save_shelf_button).setOnClickListener {
            saveShelfToFirestore()
        }

        // Shelves Image
        shelvesImage.setOnClickListener {
            selectImage()
        }

        // Delete Shelf Button
        findViewById<Button>(R.id.delete_shelf_button).setOnClickListener {
            deleteShelf()
        }
    }

    private fun fetchShelfDataFromFirestore(shelfId: String) {
        val docRef = firestore.collection("shelves").document(shelfId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val title = document.getString("title") ?: "No Title"
                val description = document.getString("description") ?: "No Description"
                existingImageUrl = document.getString("imageUrl")
                val bookIds = document.get("books") as? List<String> ?: listOf()

                // Set basic shelf data
                shelfTitle.setText(title)
                shelfDescription.setText(description)

                // Load image if exists
                existingImageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.solar_gallery_broken)
                        .into(shelvesImage)
                }

                // Fetch books data if there are book IDs
                if (bookIds.isNotEmpty()) {
                    fetchBooksData(bookIds)
                } else {
                    displayBooks.clear()
                    bookAdapter.submitList(displayBooks)
                }
            } else {
                Toast.makeText(this, "Shelf not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBooksData(bookIds: List<String>) {
        val booksCollection = firestore.collection("books")
        displayBooks.clear()

        bookIds.forEach { bookId ->
            booksCollection.document(bookId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val book = document.toObject(Books::class.java)
                        book?.let {
                            it.id = document.id
                            displayBooks.add(it)
                            // Update the RecyclerView after each book is fetched
                            bookAdapter.submitList(displayBooks.toList())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ShelvesPageActivity", "Error fetching book $bookId: ${e.message}")
                }
        }
    }

    private fun saveShelfToFirestore() {
        if (shelfId == null) {
            Toast.makeText(this, "Shelf ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val title = shelfTitle.text.toString().trim()
        val description = shelfDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val bookIds = displayBooks.mapNotNull { it.id }

        if (::selectedImageUri.isInitialized) {
            uploadImageAndSaveShelf(title, description, bookIds)
        } else {
            updateShelf(title, description, existingImageUrl, bookIds)
        }
    }

    private fun uploadImageAndSaveShelf(title: String, description: String, bookIds: List<String>) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("shelves_images/$fileName")

        storageRef.putFile(selectedImageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    updateShelf(title, description, imageUrl.toString(), bookIds)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateShelf(title: String, description: String, imageUrl: String?, bookIds: List<String>) {
        val shelfData = hashMapOf(
            "title" to title,
            "description" to description,
            "books" to bookIds
        )
        imageUrl?.let { shelfData["imageUrl"] = it }

        firestore.collection("shelves").document(shelfId!!)
            .set(shelfData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Shelf updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update shelf", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteSelectedBooks() {
        val selectedBooks = bookAdapter.getSelectedBooks()
        if (selectedBooks.isEmpty()) {
            Toast.makeText(this, "No books selected for deletion", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBookIds = selectedBooks.mapNotNull { it.id }

        firestore.collection("shelves").document(shelfId!!)
            .update("books", FieldValue.arrayRemove(*selectedBookIds.toTypedArray()))
            .addOnSuccessListener {
                displayBooks.removeAll { book -> selectedBooks.contains(book) }
                bookAdapter.submitList(displayBooks.toList())
                Toast.makeText(this, "Selected books removed from shelf", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ShelvesPageActivity", "Error removing books: ${e.message}")
                Toast.makeText(this, "Failed to remove books", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteShelf() {
        if (shelfId == null) {
            Toast.makeText(this, "Shelf ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("shelves").document(shelfId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageUrl = document.getString("imageUrl")

                    // Delete the shelf document
                    document.reference.delete()
                        .addOnSuccessListener {
                            // If successful, also delete the image if it exists
                            imageUrl?.let { url -> deleteImageFromStorage(url) }
                            Toast.makeText(this, "Shelf deleted successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete shelf", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Shelf not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error accessing shelf", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImageFromStorage(imageUrl: String) {
        try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    Log.d("ShelvesPageActivity", "Image deleted successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ShelvesPageActivity", "Failed to delete image: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("ShelvesPageActivity", "Error processing image deletion: ${e.message}")
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            1 -> { // Add books result
                if (resultCode == Activity.RESULT_OK) {
                    val newBooks = data?.getSerializableExtra("SELECTED_BOOKS") as? ArrayList<Books>
                    if (newBooks != null) {
                        // Add new books to existing list
                        val existingIds = displayBooks.mapNotNull { it.id }
                        val uniqueNewBooks = newBooks.filter { it.id !in existingIds }

                        if (uniqueNewBooks.isNotEmpty()) {
                            displayBooks.addAll(uniqueNewBooks)
                            bookAdapter.submitList(displayBooks.toList())
                            saveNewBooksToFirestore(ArrayList(uniqueNewBooks))
                        }
                    }
                }
            }
            PICK_IMAGE_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    selectedImageUri = data.data ?: return
                    shelvesImage.setImageURI(selectedImageUri)
                }
            }
        }
    }

    private fun saveNewBooksToFirestore(newBooks: ArrayList<Books>) {
        if (shelfId == null) return

        val bookIds = newBooks.mapNotNull { it.id }
        if (bookIds.isEmpty()) return

        firestore.collection("shelves").document(shelfId!!)
            .update("books", FieldValue.arrayUnion(*bookIds.toTypedArray()))
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                Log.e("ShelvesPageActivity", "Error adding new books: ${e.message}")
                Toast.makeText(this, "Failed to add new books", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("shelfId", shelfId)
    }
}