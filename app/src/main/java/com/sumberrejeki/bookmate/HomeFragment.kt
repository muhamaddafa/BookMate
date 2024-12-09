package com.sumberrejeki.bookmate

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sumberrejeki.bookmate.models.Books
import com.sumberrejeki.bookmate.models.Notes
import java.util.Locale

class HomeFragment : BaseAuthFragment() {

    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private var booksListener: ListenerRegistration? = null

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private var notesListener: ListenerRegistration? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var emptyBooksMessage: TextView
    private lateinit var emptyNotesMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyAbvM4QNppHbjLwPKmCb9kbgKCAh7ysJLE")
        }
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            Log.d(TAG, "Map is ready")
            googleMap = map
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap?.isMyLocationEnabled = true
                getCurrentLocation()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView and its adapter
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView)
        bookAdapter = BookAdapter { book ->
            navigateToBookDetail(book)
        }
        booksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        booksRecyclerView.adapter = bookAdapter

        notesRecyclerView = view.findViewById(R.id.recentNotesRecyclerView)
        noteAdapter = NoteAdapter(isHomeFragment = "recent")
        notesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        notesRecyclerView.adapter = noteAdapter

        // Fetch books for the current user
        auth.currentUser?.let { user ->
            fetchBooks(user.uid)
            fetchNotes(user.uid)
            fetchUserData()
        }

        emptyBooksMessage = view.findViewById(R.id.emptyBooksMessage)
        emptyNotesMessage = view.findViewById(R.id.emptyNotesMessage)
    }

    private fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val username = document.getString("displayName")
                    val photoUrl = document.getString("photoUrl") // Tambahkan ini

                    // Update UI dengan data yang diambil
                    view?.findViewById<TextView>(R.id.greetingTextView)?.setText("Hi, $username!")

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
                Log.d("ProfileFragment", "get failed with ", exception)
            }
        }
    }

    private fun fetchNotes(userId: String) {
        notesListener = db.collection("notes")
            .whereEqualTo("userId", userId)
            .limit(4)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notes = snapshot.toObjects(Notes::class.java)
                    noteAdapter.submitList(notes)

                    if (notes.isEmpty()) {
                        notesRecyclerView.visibility = View.GONE
                        emptyNotesMessage.visibility = View.VISIBLE
                    } else {
                        notesRecyclerView.visibility = View.VISIBLE
                        emptyNotesMessage.visibility = View.GONE
                    }
                }
            }
    }

    private fun fetchBooks(userId: String) {
        booksListener = db.collection("books")
            .whereEqualTo("userId", userId) // Filter books by user ID
            .limit(4)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val books = snapshot.toObjects(Books::class.java)
                    bookAdapter.submitList(books)

                    if (books.isEmpty()) {
                        booksRecyclerView.visibility = View.GONE
                        emptyBooksMessage.visibility = View.VISIBLE
                    } else {
                        booksRecyclerView.visibility = View.VISIBLE
                        emptyBooksMessage.visibility = View.GONE
                    }
                }
            }
    }

    private fun navigateToBookDetail(book: Books) {
        // Create an Intent to start the BookDetailActivity
        val intent = Intent(requireContext(), BookScreenActivity::class.java)
        intent.putExtra("BOOK_TITLE", book.title)
        // Add any other data you want to pass
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        booksListener?.remove()
        notesListener?.remove()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                addNearbyBookstores(currentLatLng)
            }
        }
    }

    private fun addNearbyBookstores(currentLatLng: LatLng) {
        val placesClient = Places.createClient(requireContext())

        val request = FindCurrentPlaceRequest.newInstance(
            listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES)
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission not granted
            return
        }

        placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
            for (placeLikelihood in response.placeLikelihoods) {
                val place = placeLikelihood.place
                val isBookstore = place.types?.contains(Place.Type.BOOK_STORE) ?: false
                val nameContainsBookstore = place.name?.toLowerCase(Locale.getDefault())?.let { name ->
                    name.contains("book") || name.contains("toko buku") ||
                            name.contains("gramedia") || name.contains("perpustakaan")
                } ?: false

                if (isBookstore || nameContainsBookstore) {
                    place.latLng?.let { latLng ->
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(place.name)
                        )
                    }
                    Log.d("PlacesAPI", "Found bookstore: ${place.name}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("PlacesAPI", "Error finding bookstores", exception)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        googleMap?.isMyLocationEnabled = true
                        getCurrentLocation()
                    }
                } else {
                    Toast.makeText(requireContext(), "Location permission is required for this feature", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
            }
        }
    }


    companion object {
        private const val TAG = "HomeFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
