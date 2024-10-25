package com.sumberrejeki.bookmate

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        val db = FirebaseFirestore.getInstance()

        val checkIcon: ImageView = findViewById(R.id.check_icon)
        val dateTimeContainer: LinearLayout = findViewById(R.id.dateTimeContainer)
        val dateTextView: TextView = findViewById(R.id.date)
        val timeTextView: TextView = findViewById(R.id.time)

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val note = hashMapOf(
            "date" to currentDate,
            "time" to currentTime,
            "description" to findViewById<EditText>(R.id.notes_description).text.toString()
        )

        db.collection("users").document("user_id").collection("notes")
            .add(note)
            .addOnSuccessListener { documentReference ->
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle failure
            }

        checkIcon.setOnClickListener {
            dateTimeContainer.visibility = LinearLayout.VISIBLE
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            dateTextView.text = currentDate
            timeTextView.text = currentTime
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}