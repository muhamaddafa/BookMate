package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookScreenActivity : AppCompatActivity() {

    private lateinit var editBookButton: ImageView
    private lateinit var addNoteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_book_screen)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        editBookButton = findViewById(R.id.editBookButton)
        editBookButton.setOnClickListener {
            val intent = Intent(this, EditBookActivity::class.java)
            startActivity(intent)
        }

        addNoteButton = findViewById(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            val intent = Intent(this, AddNotesActivity::class.java)
            startActivity(intent)
        }
    }
}