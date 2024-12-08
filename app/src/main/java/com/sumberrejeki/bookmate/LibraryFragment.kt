package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Shelf

class LibraryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var shelvesAdapter: ShelvesAdapter
    private val shelvesList = mutableListOf<Shelf>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        shelvesAdapter = ShelvesAdapter(shelvesList)
        recyclerView.adapter = shelvesAdapter

        // Button BookList
        val bookListButton: View = view.findViewById(R.id.bookListButton)
        bookListButton.setOnClickListener {
            val intent = Intent(requireContext(), BookListActivity::class.java)
            startActivity(intent)
        }

        // Button untuk berpindah ke AddShelvesActivity
        val newShelvesButton: View = view.findViewById(R.id.newShelvesButton)
        newShelvesButton.setOnClickListener {
            val intent = Intent(requireContext(), AddShelvesActivity::class.java)
            startActivity(intent)
        }

        // Fetch data from Firestore
        auth.currentUser?.let { user ->
            fetchShelvesData(user.uid)
        }
    }

    private fun fetchShelvesData(userId: String) {
        firestore.collection("shelves")
            .whereEqualTo("userId", userId) // Hanya ambil data dengan userId sesuai
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Toast.makeText(requireContext(), "Failed to fetch shelves: ${exception.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    shelvesList.clear()
                    for (document in querySnapshot) {
                        val shelf = Shelf(
                            id = document.id,
                            imageUrl = document.getString("imageUrl") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            userId = document.getString("userId") ?: "" // Pastikan userId diisi
                        )
                        shelvesList.add(shelf)
                    }
                    if (shelvesList.isEmpty()) {
                        Toast.makeText(requireContext(), "No shelves available", Toast.LENGTH_SHORT).show()
                    }
                    shelvesAdapter.notifyDataSetChanged()
                }
            }
    }

}
