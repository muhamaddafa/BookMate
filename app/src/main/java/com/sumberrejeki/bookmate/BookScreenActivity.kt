package com.sumberrejeki.bookmate

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sumberrejeki.bookmate.models.Books
import java.util.concurrent.Executors

class BookScreenActivity : AppCompatActivity() {

    private lateinit var editBookButton: ImageView
    private lateinit var addNoteButton: Button
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_GALLERY = 2
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_screen)

        // Initialize toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Initialize buttons
        editBookButton = findViewById(R.id.editBookButton)
        editBookButton.setOnClickListener {
            val id = intent.getStringExtra("BOOK_TITLE") ?: "No Title"
            val intent = Intent(this, EditBookActivity::class.java)
            intent.putExtra("BOOK_TITLE", title)
            startActivity(intent)
        }

        addNoteButton = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            showImageSourceDialog()
        }

        // Fetch books data
        fetchBooks()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image Source")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    // Camera option selected
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
                1 -> {
                    // Gallery option selected
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        processImage(imageBitmap)
                    } else {
                        Log.e("BookScreenActivity", "Failed to retrieve image bitmap")
                    }
                }
                REQUEST_IMAGE_GALLERY -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                        processImage(imageBitmap)
                    } ?: Log.e("BookScreenActivity", "Failed to retrieve image URI")
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = TextRecognizerOptions.Builder()
            .setExecutor(Executors.newSingleThreadExecutor())
            .build()
        val recognizer = TextRecognition.getClient(options)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                val title = intent.getStringExtra("BOOK_TITLE") ?: "No Title"
                val intent = Intent(this, AddNotesActivity::class.java)
                intent.putExtra("OCR_TEXT", recognizedText)
                intent.putExtra("BOOK_ID", bookId)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("BookScreenActivity", "Text recognition failed: ", e)
            }
    }

    private fun getImageOrientation(imageUri: Uri): Int {
        val inputStream = contentResolver.openInputStream(imageUri)
        val exif = ExifInterface(inputStream!!)
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    private fun rotateImageIfRequired(image: Bitmap, uri: Uri): Bitmap {
        val orientation = getImageOrientation(uri)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(image, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(image, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(image, 270f)
            else -> image // No rotation required
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun fetchBooks() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val title = intent.getStringExtra("BOOK_TITLE") ?: "No Title"
            val docRef = db.collection("books").whereEqualTo("title", title)

            docRef.get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        bookId = document.id
                        val title = document.getString("title") ?: "No Title"
                        val author = document.getString("author") ?: "Unknown Author"
                        val publisher = document.getString("publisher") ?: "Unknown Publisher"
                        val totalPages = document.getLong("pages") ?: 0
                        val imageUrl = document.getString("imageUrl")
                        val desc = document.getString("description") ?: "No Description"

                        // Update UI with book data
                        findViewById<TextView>(R.id.titleTextView)?.text = title
                        findViewById<TextView>(R.id.authorTextView)?.text = author
                        findViewById<TextView>(R.id.publisherTextView)?.text = publisher
                        findViewById<TextView>(R.id.totalPagesTextView)?.text = totalPages.toString()
                        findViewById<TextView>(R.id.descriptionTextView)?.text = desc

                        // Load image using Glide
                        val imageView = findViewById<ImageView>(R.id.bookImageView)
                        imageUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.image_placeholder)
                                .error(R.drawable.image_placeholder)
                                .into(imageView)
                        }
                    }
                } else {
                    Log.d("BookScreenActivity", "No books found for user: $userId")
                }
            }.addOnFailureListener { exception ->
                Log.e("BookScreenActivity", "Error getting documents: ", exception)
            }
        } ?: run {
            Log.w("BookScreenActivity", "User is not logged in.")
        }
    }
}
