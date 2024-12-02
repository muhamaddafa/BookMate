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
import java.util.*

class AddShelvesActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private lateinit var shelvesImage: ImageView
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
                shelvesImage.setImageURI(selectedImageUri)  // Menampilkan gambar di ImageView
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

        val storageRef = storage.reference.child("shelves_images/${UUID.randomUUID()}.jpg")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    addShelfToFirestore(title, description, imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Tambah rak ke Firestore
    private fun addShelfToFirestore(title: String, description: String, imageUrl: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        val shelfData = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "userID" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("shelves").document(title)
            .set(shelfData)
            .addOnSuccessListener {
                Toast.makeText(this, "Shelf added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add shelf: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
