package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class LibraryFragment : Fragment() {

    private lateinit var bookListButton : Button
    private lateinit var addShelvesButton : Button
    private lateinit var editShelvesButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookListButton = view.findViewById(R.id.bookListButton)
        addShelvesButton = view.findViewById(R.id.newShelvesButton)
        editShelvesButton = view.findViewById(R.id.editButton1)

        bookListButton.setOnClickListener {
            val intent = Intent(activity, BookListActivity::class.java)
            startActivity(intent)
        }

        addShelvesButton.setOnClickListener {
            val intent = Intent(activity, AddShelvesActivity::class.java)
            startActivity(intent)
        }

        editShelvesButton.setOnClickListener {
            val intent = Intent(activity, ShelvesPageActivity::class.java)
            startActivity(intent)
        }
    }

}