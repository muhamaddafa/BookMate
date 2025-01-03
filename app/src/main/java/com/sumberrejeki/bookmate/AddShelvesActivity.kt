package com.sumberrejeki.bookmate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddShelvesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private lateinit var shelvesImage: ImageView
    private lateinit var progressBar: ProgressBar
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_shelves)

        // Initialize Firebase services
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set up views
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val addTitleLabel = findViewById<EditText>(R.id.add_title_label)
        val addDescriptionLabel = findViewById<EditText>(R.id.add_description_label)
        val addShelvesButton = findViewById<Button>(R.id.add_shelves)
        shelvesImage = findViewById(R.id.add_shelves_image)
        progressBar = findViewById(R.id.progressBar)

        // Toolbar navigation
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Image picker
        shelvesImage.setOnClickListener {
            openGallery()
        }

        // Add shelves button
        addShelvesButton.setOnClickListener {
            val title = addTitleLabel.text.toString().trim()
            val description = addDescriptionLabel.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (description.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageAndAddShelfToFirestore(title, description)
            }
        }
    }

    // Fungsi untuk memilih gambar dari galeri
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Mendapatkan hasil gambar yang dipilih
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                // Menampilkan gambar di ImageView
                shelvesImage.setImageURI(selectedImageUri)

                // Mengubah scaleType menjadi fitCenter setelah gambar diunggah
                shelvesImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Upload gambar ke Firebase Storage dan tambah rak ke Firestore
    private fun uploadImageAndAddShelfToFirestore(title: String, description: String) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan ProgressBar
        progressBar.visibility = View.VISIBLE

        // Generate unique file name
        val uniqueFileName = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("shelves_images/$uniqueFileName.jpg")

        // Mulai upload file
        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // Ambil URL dari gambar yang diunggah
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Tambahkan rak ke Firestore
                    addShelfToFirestore(title, description, imageUrl)

                    // Reset selectedImageUri setelah berhasil
                    selectedImageUri = null
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Sembunyikan ProgressBar saat gagal
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Tambah rak ke Firestore
    private fun addShelfToFirestore(title: String, description: String, imageUrl: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Generate ID unik untuk rak
        val newShelf = firestore.collection("shelves").document()
        val shelfId = newShelf.id

        val shelfData = hashMapOf(
            "id" to shelfId, // Tambahkan shelfId
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        newShelf
            .set(shelfData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE // Sembunyikan ProgressBar saat sukses
                Toast.makeText(this, "Shelf added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE // Sembunyikan ProgressBar saat gagal
                Toast.makeText(this, "Failed to add shelf: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
