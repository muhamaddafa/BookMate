package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BookListActivity : AppCompatActivity() {

    private lateinit var bookButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_book_list)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bookButton = findViewById(R.id.book1)
        bookButton.setOnClickListener {
            val intent = Intent(this, BookScreenActivity::class.java)
            startActivity(intent)
        }
    }
}