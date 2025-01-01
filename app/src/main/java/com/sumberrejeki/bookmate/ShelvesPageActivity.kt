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
import android.widget.ProgressBar
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

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        shelfTitle = findViewById(R.id.shelf_title)
        shelfDescription = findViewById(R.id.shelf_description)

//        this.shelfId = intent.getStringExtra("shelfId")
        // Ambil dari savedInstanceState jika ada, jika tidak ambil dari Intent
        shelfId = savedInstanceState?.getString("shelfId") ?: intent.getStringExtra("shelfId")
        Log.d("ShelvesPageActivity", "Shelf ID: $shelfId") // Tambahkan di sini
        if (shelfId != null) {
            fetchShelfDataFromFirestore(shelfId!!) // Load data using shelfId
        } else {
            Log.e("ShelvesPageActivity", "No Shelf ID received!")
        }

        val addBookButton: Button = findViewById(R.id.add_book_button)
        addBookButton.setOnClickListener {
            val intent = Intent(this, BookListShelves::class.java)
            intent.putExtra("shelfId", shelfId)  // Kirim shelfId saat pindah ke BookListShelves
            startActivityForResult(intent, 1)  // Kirim dengan request code 1
//            startActivity(intent)
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

        val saveShelfButton: Button = findViewById(R.id.save_shelf_button)
        saveShelfButton.setOnClickListener {
            saveShelfToFirestore()
        }

        shelvesImage = findViewById(R.id.shelves_image)
        shelvesImage.setOnClickListener {
            selectImage()
        }

        val deleteShelfButton: Button = findViewById(R.id.delete_shelf_button)
        deleteShelfButton.setOnClickListener {
            deleteShelf()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("shelfId", shelfId)
    }

    // Simpan perubahan shelf ke Firestore
    private fun saveShelfToFirestore() {
        Log.d("ShelvesPageActivity", "Shelf ID (saveShelfToFirestore): $shelfId")
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

        // Ambil ID buku yang dipilih
        val bookIds = displayBooks.mapNotNull { it.id }

        if (::selectedImageUri.isInitialized && selectedImageUri != null) {
            // Upload gambar baru dan update shelf
            uploadImageAndSaveShelf(title, description, bookIds)
        } else {
            // Update langsung tanpa upload gambar
            updateShelf(title, description, existingImageUrl, bookIds)
        }
    }

    // Upload gambar dan update shelf di Firestore
    private fun uploadImageAndSaveShelf(
        title: String,
        description: String,
        bookIds: List<String>
    ) {
        val uri = selectedImageUri ?: return
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("shelves_images/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    updateShelf(title, description, imageUrl.toString(), bookIds)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    // Update shelf dengan semua data
    private fun updateShelf(
        title: String,
        description: String,
        imageUrl: String?,
        bookIds: List<String>
    ) {
        val updatedData = hashMapOf<String, Any>(
            "title" to title,
            "description" to description
        )

        imageUrl?.let {
            updatedData["imageUrl"] = it
        }

        // Tambahkan hanya title, description, dan imageUrl
        firestore.collection("shelves").document(shelfId!!)
            .set(updatedData, SetOptions.merge())
            .addOnSuccessListener {
                // Setelah sukses memperbarui title dan description, tambahkan buku ke array
                if (bookIds.isNotEmpty()) {
                    firestore.collection("shelves").document(shelfId!!)
                        .update("books", FieldValue.arrayUnion(*bookIds.toTypedArray()))  // arrayUnion mencegah duplikat
                        .addOnSuccessListener {
                            Toast.makeText(this, "Books added to shelf!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add books to shelf", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update shelf", Toast.LENGTH_SHORT).show()
            }
    }





    // Fungsi untuk mengambil data dari Firestore
    private fun fetchShelfDataFromFirestore(shelfId: String) {
        val docRef = firestore.collection("shelves").document(shelfId)
        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val title = document.getString("title") ?: "No Title"
                val description = document.getString("description") ?: "No Description"
                existingImageUrl = document.getString("imageUrl")

                findViewById<EditText>(R.id.shelf_title).setText(title)
                findViewById<EditText>(R.id.shelf_description).setText(description)

                existingImageUrl?.let { url ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.solar_gallery_broken)
                        .into(shelvesImage)
                }
            } else {
                Toast.makeText(this, "Shelf not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }


    // Pilih gambar baru dari galeri
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Tangani hasil gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            shelfId = data?.getStringExtra("shelfId")  // Dapatkan shelfId
            Log.d("ShelvesPageActivity", "Shelf ID: $shelfId") // Tambahkan di sini
            val selectedBooks = data?.getSerializableExtra("SELECTED_BOOKS") as? ArrayList<Books>
            if (selectedBooks != null) {
                displayBooks.clear()
                displayBooks.addAll(selectedBooks)
                bookAdapter.submitList(displayBooks)
                // Log jumlah buku dan ID buku yang dipilih
                Log.d("ShelvesPageActivity", "Received Books: ${selectedBooks.size} books, IDs: ${selectedBooks.map { it.id }}")
            }else {
                Log.d("ShelvesPageActivity", "No books selected, Shelf ID: $shelfId")
            }
//            Log.d("ShelvesPageActivity", "Received Books: ${selectedBooks?.size} books, Shelf ID: $shelfId")
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("ShelvesPageActivity", "Shelf ID: $shelfId") // Tambahkan di sini
            selectedImageUri = data.data ?: return
            shelvesImage.setImageURI(selectedImageUri)
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

    private fun deleteImageFromStorage(imageUrl: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("ShelvesPageActivity", "Image deleted successfully!")
            }
            .addOnFailureListener {
                Log.e("ShelvesPageActivity", "Failed to delete image from storage: ${it.message}")
            }
    }


    private fun deleteShelf() {
        Log.d("ShelvesPageActivity", "Shelf ID (deleteShelfFromFirestore): $shelfId")
        if (shelfId == null) {
            Toast.makeText(this, "Shelf ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val docRef = firestore.collection("shelves").document(shelfId!!)

        docRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val imageUrl = document.getString("imageUrl")

                // Hapus dokumen shelf dari Firestore
                docRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Shelf deleted successfully!", Toast.LENGTH_SHORT).show()

                        // Hapus gambar dari Firebase Storage jika ada
                        imageUrl?.let { url ->
                            deleteImageFromStorage(url)
                        }

                        // Tutup activity setelah penghapusan
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete shelf", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Shelf not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching shelf data", Toast.LENGTH_SHORT).show()
        }
    }

}


