package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignupActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnSignup: Button
    private lateinit var linkLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        username = findViewById(R.id.usernameInput)
        firstName = findViewById(R.id.firstNameInput)
        lastName = findViewById(R.id.lastNameInput)
        email = findViewById(R.id.emailInput)
        password = findViewById(R.id.passwordInput)
        btnSignup = findViewById(R.id.signupButton)
        linkLogin = findViewById(R.id.loginLink)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // Request profile information
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setHintBehavior(username)
        setHintBehavior(firstName)
        setHintBehavior(lastName)
        setHintBehavior(email)
        setHintBehavior(password)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnSignup.setOnClickListener {
            signupUser()
        }

        findViewById<LinearLayout>(R.id.googleSignInButton).setOnClickListener {
            signInWithGoogle()
        }

        linkLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setHintBehavior(editText: EditText) {
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                editText.hint = "" // Hilangkan hint saat EditText difokuskan
            } else if (editText.text.toString().isEmpty()) {
                // Kembalikan hint jika EditText tidak ada isinya
                editText.hint = when (editText.id) {
                    R.id.usernameInput -> "Enter Username"
                    R.id.firstNameInput -> "Enter First Name"
                    R.id.lastNameInput -> "Enter Last Name"
                    R.id.emailInput -> "Enter Email"
                    R.id.passwordInput -> "Enter Password"
                    else -> ""
                }
            }
        }
    }

    private fun saveUserToFirestore(userId: String, userData: Map<String, Any>) {
        db.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "User data saved successfully")
                proceedToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user data", e)
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                proceedToMainActivity()
            }
    }

    private fun proceedToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun signupUser() {
        val usernameStr = username.text.toString().trim()
        val firstNameStr = firstName.text.toString().trim()
        val lastNameStr = lastName.text.toString().trim()
        val emailStr = email.text.toString().trim()
        val passwordStr = password.text.toString().trim()

        if (usernameStr.isEmpty() || firstNameStr.isEmpty() || lastNameStr.isEmpty() || emailStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignup.isEnabled = false

        auth.createUserWithEmailAndPassword(emailStr, passwordStr)
            .addOnCompleteListener { task ->
                btnSignup.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Create user data for Firestore
                        val userData = hashMapOf(
                            "displayName" to usernameStr,
                            "firstName" to firstNameStr,
                            "lastName" to lastNameStr,
                            "email" to emailStr,
                            "createdAt" to System.currentTimeMillis(),
                            "lastLoginAt" to System.currentTimeMillis(),
                            "signupMethod" to "email"
                        )

                        saveUserToFirestore(user.uid, userData)
                        Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Your Email is Already Registered", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, account)
        } catch (e: ApiException) {
            Log.e("Google Sign In", "Google sign in failed", e)
            Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Create user data with Google profile information
                        val userData = hashMapOf(
                            "email" to (account.email ?: ""),
                            "firstName" to (account.givenName ?: ""),
                            "lastName" to (account.familyName ?: ""),
                            "displayName" to (account.displayName ?: ""),
                            "photoUrl" to (account.photoUrl?.toString() ?: ""),
                            "createdAt" to System.currentTimeMillis(),
                            "lastLoginAt" to System.currentTimeMillis(),
                            "signupMethod" to "google"
                        )

                        saveUserToFirestore(user.uid, userData)
                    } else {
                        Toast.makeText(this, "Error: Unable to get user data", Toast.LENGTH_SHORT).show()
                        proceedToMainActivity()
                    }
                } else {
                    Log.e("Google Auth", "Authentication failed", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}