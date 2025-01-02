package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : BaseAuthFragment() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var changePasswordLink: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchUserData()
        view.findViewById<TextView>(R.id.changePasswordLink).setOnClickListener {
            startActivity(Intent(activity, ForgotpasswordActivity::class.java))
        }
        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logoutUser()
        }
        view.findViewById<Button>(R.id.saveButton).setOnClickListener {
            updateUserData()
        }

        setHintBehavior(view.findViewById(R.id.usernameInput))
        setHintBehavior(view.findViewById(R.id.firstNameInput))
        setHintBehavior(view.findViewById(R.id.lastNameInput))
        setHintBehavior(view.findViewById(R.id.emailInput))
        setHintBehavior(view.findViewById(R.id.addressInput))
        setHintBehavior(view.findViewById(R.id.cityInput))
        setHintBehavior(view.findViewById(R.id.stateInput))
        setHintBehavior(view.findViewById(R.id.zipCodeInput))
        setHintBehavior(view.findViewById(R.id.countryInput))
    }

    private fun setHintBehavior(editText: EditText) {
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                editText.hint = "" // Hilangkan hint saat EditText difokuskan
            } else if (editText.text.toString().isEmpty()) {
                // Kembalikan hint jika EditText tidak ada isinya
                editText.hint = when (editText.id) {
                    R.id.usernameInput -> "Input Username"
                    R.id.firstNameInput -> "Input First Name"
                    R.id.lastNameInput -> "Input Last Name"
                    R.id.emailInput -> "Input Email"
                    R.id.addressInput -> "Input Address"
                    R.id.cityInput -> "Input City"
                    R.id.stateInput -> "Input State"
                    R.id.zipCodeInput -> "Input Zip Code"
                    R.id.countryInput -> "Enter Country"
                    else -> ""
                }
            }
        }
    }

    private fun updateUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            val userId = firebaseUser.uid

            // First, get the current document to preserve photoUrl
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    // Get the existing photoUrl
                    val existingPhotoUrl = document.getString("photoUrl")

                    // Create userData map with all fields
                    val userData = hashMapOf(
                        "displayName" to view?.findViewById<TextInputEditText>(R.id.usernameInput)?.text.toString(),
                        "firstName" to view?.findViewById<TextInputEditText>(R.id.firstNameInput)?.text.toString(),
                        "lastName" to view?.findViewById<TextInputEditText>(R.id.lastNameInput)?.text.toString(),
                        "email" to view?.findViewById<TextInputEditText>(R.id.emailInput)?.text.toString(),
                        "address" to view?.findViewById<TextInputEditText>(R.id.addressInput)?.text.toString(),
                        "city" to view?.findViewById<TextInputEditText>(R.id.cityInput)?.text.toString(),
                        "state" to view?.findViewById<TextInputEditText>(R.id.stateInput)?.text.toString(),
                        "zipCode" to view?.findViewById<TextInputEditText>(R.id.zipCodeInput)?.text.toString(),
                        "country" to view?.findViewById<TextInputEditText>(R.id.countryInput)?.text.toString()
                    )

                    // Add the existing photoUrl to the userData map if it exists
                    existingPhotoUrl?.let {
                        userData["photoUrl"] = it
                    }

                    // Update the document with the new data
                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Data updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(activity, "Error updating data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error retrieving current data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
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
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    val email = document.getString("email")
                    val address = document.getString("address")
                    val city = document.getString("city")
                    val state = document.getString("state")
                    val zipCode = document.getString("zipCode")
                    val country = document.getString("country")
                    val photoUrl = document.getString("photoUrl") // Tambahkan ini

                    // Update UI dengan data yang diambil
                    view?.findViewById<TextInputEditText>(R.id.usernameInput)?.setText(username)
                    view?.findViewById<TextInputEditText>(R.id.firstNameInput)?.setText(firstName)
                    view?.findViewById<TextInputEditText>(R.id.lastNameInput)?.setText(lastName)
                    view?.findViewById<TextInputEditText>(R.id.emailInput)?.setText(email)
                    view?.findViewById<TextInputEditText>(R.id.addressInput)?.setText(address)
                    view?.findViewById<TextInputEditText>(R.id.cityInput)?.setText(city)
                    view?.findViewById<TextInputEditText>(R.id.stateInput)?.setText(state)
                    view?.findViewById<TextInputEditText>(R.id.zipCodeInput)?.setText(zipCode)
                    view?.findViewById<TextInputEditText>(R.id.countryInput)?.setText(country)

                    // Memuat gambar menggunakan Glide
                    val imageView = view?.findViewById<ImageView>(R.id.imageView)
                    imageView?.let {
                        Glide.with(this)
                            .load(photoUrl)
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

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}