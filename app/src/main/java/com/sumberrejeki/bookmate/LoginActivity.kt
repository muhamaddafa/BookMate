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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var linkForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var linkSignup: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase components first
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI components
        email = findViewById(R.id.emailInput)
        password = findViewById(R.id.passwordInput)
        linkForgotPassword = findViewById(R.id.forgotPasswordLink)
        btnLogin = findViewById(R.id.loginButton)
        linkSignup = findViewById(R.id.signupLink)

        if (auth.currentUser != null) {
            // Pengguna sudah login, langsung pindah ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Menutup LoginActivity
            return // Keluar dari onCreate untuk mencegah loading lebih lanjut
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // Request profile information
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        linkForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotpasswordActivity::class.java))
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        findViewById<LinearLayout>(R.id.googleButton).setOnClickListener {
            signInWithGoogle()
        }

        linkSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun saveUserToFirestore(userId: String, userData: Map<String, Any>) {
        // Reference to the user document
        val userRef = db.collection("users").document(userId)

        // First check if the document exists
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // If exists, update lastLoginAt and any missing profile data
                    userRef.set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Firestore", "User data updated successfully")
                            proceedToMainActivity()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error updating user data", e)
                            proceedToMainActivity()
                        }
                } else {
                    // If doesn't exist, create new document
                    userRef.set(userData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "New user data saved successfully")
                            proceedToMainActivity()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error saving new user data", e)
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            proceedToMainActivity()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking user document", e)
                Toast.makeText(this, "Error checking user data: ${e.message}", Toast.LENGTH_SHORT).show()
                proceedToMainActivity()
            }
    }

    private fun proceedToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginUser() {
        val emailStr = email.text.toString().trim()
        val passwordStr = password.text.toString().trim()

        if (emailStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(this, "Email and Password must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(emailStr, passwordStr)
            .addOnCompleteListener { task ->
                btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "email" to emailStr,
                            "lastLoginAt" to System.currentTimeMillis()
                        )
                        saveUserToFirestore(user.uid, userData)
                    } else {
                        Toast.makeText(this, "Error: User is null after login", Toast.LENGTH_SHORT).show()
                        proceedToMainActivity()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!, account)
                }
            } catch (e: ApiException) {
                Log.e("Google Sign In", "Google sign in failed", e)
                Toast.makeText(this, "Google Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Create user data map with Google profile information
                        val userData = hashMapOf(
                            "email" to (account.email ?: ""),
                            "firstName" to (account.givenName ?: ""),
                            "lastName" to (account.familyName ?: ""),
                            "displayName" to (account.displayName ?: ""),
                            "photoUrl" to (account.photoUrl?.toString() ?: ""),
                            "lastLoginAt" to System.currentTimeMillis(),
                            "createdAt" to System.currentTimeMillis(),
                            "signupMethod" to "google"
                        )

                        saveUserToFirestore(user.uid, userData)
                    } else {
                        Toast.makeText(this, "Error: Unable to get user data", Toast.LENGTH_SHORT).show()
                        proceedToMainActivity()
                    }
                } else {
                    Log.e("Google Auth", "firebaseAuthWithGoogle failed", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}