package com.sumberrejeki.bookmate

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Notes
import java.text.SimpleDateFormat
import java.util.*

class AddNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val checkIcon: ImageView = findViewById(R.id.check_icon)
        val dateTimeContainer: LinearLayout = findViewById(R.id.dateTimeContainer)
        val dateTextView: TextView = findViewById(R.id.date)
        val timeTextView: TextView = findViewById(R.id.time)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Get OCR text and book ID from intent
        val ocrText = intent.getStringExtra("OCR_TEXT") ?: ""
        val bookId = intent.getStringExtra("BOOK_ID") ?: ""
        val userId = auth.currentUser?.uid // Get the current user's ID

        // Set the OCR text in the EditText
        findViewById<EditText>(R.id.notes_description).setText(ocrText)

        // Create a Notes object
        val note = Notes(
            bookId = bookId,
            userId = userId,
            text = findViewById<EditText>(R.id.notes_description).text.toString(),
            created = Date()
        )

        checkIcon.setOnClickListener {
            dateTimeContainer.visibility = LinearLayout.VISIBLE
            dateTextView.text = currentDate
            timeTextView.text = currentTime

            db.collection("notes")
                .add(note)
                .addOnSuccessListener { documentReference ->
                    Log.d("Edit", "Berhasil Input Notes")
                }
                .addOnFailureListener { e ->
                    Log.d("Edit", "Gagal Input Notes")
                }
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}