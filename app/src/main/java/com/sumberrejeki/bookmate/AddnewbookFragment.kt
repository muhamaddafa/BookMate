package com.sumberrejeki.bookmate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.sumberrejeki.bookmate.R

class AddnewbookFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_addnewbook, container, false)

        // Initialize views
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        val bookTitle: TextInputEditText = view.findViewById(R.id.bookTitle)
        val bookAuthor: TextInputEditText = view.findViewById(R.id.bookAuthor)
        val bookPublisher: TextInputEditText = view.findViewById(R.id.bookPublisher)
        val bookPages: TextInputEditText = view.findViewById(R.id.bookPages)
        val bookDescription: EditText = view.findViewById(R.id.bookDescription)
        val paperBookButton: Button = view.findViewById(R.id.paperBook)
        val eBookButton: Button = view.findViewById(R.id.eBook)
        val addBookButton: Button = view.findViewById(R.id.addBookButton)
        val bookImage: ImageView = view.findViewById(R.id.bookImage)
        val cameraButton: ImageView = view.findViewById(R.id.cameraButton)

        // Set toolbar navigation click listener
        toolbar.setNavigationOnClickListener {
            // Handle back button click
            requireActivity().onBackPressed()
        }

        // Set button click listeners
        paperBookButton.setOnClickListener {
            // Handle paper book button click
        }

        eBookButton.setOnClickListener {
            // Handle e-book button click
        }

        addBookButton.setOnClickListener {
            // Handle add book button click
            val title = bookTitle.text.toString()
            val author = bookAuthor.text.toString()
            val publisher = bookPublisher.text.toString()
            val pages = bookPages.text.toString()
            val description = bookDescription.text.toString()

            // Add logic to save the book details
        }

        cameraButton.setOnClickListener {
            // Handle camera button click
        }

        return view
    }
}