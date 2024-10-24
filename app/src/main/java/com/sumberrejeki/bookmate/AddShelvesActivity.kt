package com.sumberrejeki.bookmate

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AddShelvesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_shelves)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val addTitleLabel: EditText = findViewById(R.id.add_title_label)
        val addBookButton: ImageView = findViewById(R.id.add_book_button)
        val scrollViewLayout: LinearLayout = findViewById(R.id.scrollView)

        addBookButton.setOnClickListener {
            addBookToShelves(scrollViewLayout)
        }
    }

    private fun addBookToShelves(scrollViewLayout: LinearLayout) {
        val newBookImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(150, 200).apply {
                setMargins(10, 10, 10, 10)
            }
            setBackgroundResource(R.drawable.border_2px)
            setPadding(20, 20, 20, 20)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(R.drawable.image_placeholder)
        }

        val childCount = scrollViewLayout.childCount
        if (childCount % 2 == 0) {
            val newRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }
            newRow.addView(newBookImageView)
            scrollViewLayout.addView(newRow)
        } else {
            val lastRow = scrollViewLayout.getChildAt(childCount - 1) as LinearLayout
            lastRow.addView(newBookImageView)
        }
    }
}