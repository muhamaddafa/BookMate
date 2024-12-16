package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sumberrejeki.bookmate.models.Shelf

class LibraryFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var shelvesAdapter: ShelvesAdapter
    private val shelvesList = mutableListOf<Shelf>()

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

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

        val searchEditText : EditText = view.findViewById(R.id.searchLibrary)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    filterShelves(s.toString())
                }

                handler.postDelayed(searchRunnable!!, 1500)
            }
        })

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

        // Fetch user data
        fetchUserData()
    }

    private fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val photoUrl = document.getString("photoUrl") // Tambahkan ini

                    // Memuat gambar menggunakan Glide
                    val imageView = view?.findViewById<ImageView>(R.id.avatarImageView)
                    imageView?.let {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.image_placeholder) // Gambar placeholder saat loading
                            .error(R.drawable.image_placeholder) // Gambar error jika gagal memuat
                            .into(it) // 'it' merujuk pada imageView yang non-null
                    }
                }
            }.addOnFailureListener { exception ->
                Log.d("LibraryFragment", "get failed with ", exception)
            }
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

    private fun filterShelves(query: String) {
        val filteredList = shelvesList.filter { shelf ->
            (shelf.title?.contains(query, ignoreCase = true) == true) ||
                    (shelf.description?.contains(query, ignoreCase = true) == true)
        }
        shelvesAdapter.updateList(filteredList)
    }

}
