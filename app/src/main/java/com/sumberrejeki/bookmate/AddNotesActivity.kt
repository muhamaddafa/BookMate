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

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var notesDescriptionEditText: EditText
    private lateinit var dateTimeContainer: LinearLayout
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var checkIcon: ImageView

    private val currentDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private val currentTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        initializeFirebase()
        initializeViews()
        setupToolbar()
        setupNotesDescription()
        setupCheckIcon()
    }

    private fun initializeFirebase() {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun initializeViews() {
        checkIcon = findViewById(R.id.check_icon)
        dateTimeContainer = findViewById(R.id.dateTimeContainer)
        dateTextView = findViewById(R.id.date)
        timeTextView = findViewById(R.id.time)
        notesDescriptionEditText = findViewById(R.id.notes_description)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            saveNote()
            onBackPressed()
        }
    }

    private fun setupNotesDescription() {
        val ocrText = intent.getStringExtra("OCR_TEXT") ?: ""
        notesDescriptionEditText.setText(ocrText)
        notesDescriptionEditText.requestFocus()
    }

    private fun setupCheckIcon() {
        checkIcon.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        dateTimeContainer.visibility = LinearLayout.VISIBLE
        dateTextView.text = currentDate
        timeTextView.text = currentTime

        val note = createNote()
        db.collection("notes")
            .add(note)
            .addOnSuccessListener {
                Log.d("Edit", "Berhasil Input Notes")
                onBackPressed()
            }
            .addOnFailureListener { e ->
                Log.d("Edit", "Gagal Input Notes: ${e.message}")
            }
    }

    private fun createNote(): Notes {
        val bookId = intent.getStringExtra("BOOK_ID") ?: ""
        val userId = auth.currentUser?.uid ?: ""
        return Notes(
            bookId = bookId,
            userId = userId,
            text = notesDescriptionEditText.text.toString(),
            created = Date()
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        saveNote() // Save note when the activity is paused
    }
}
