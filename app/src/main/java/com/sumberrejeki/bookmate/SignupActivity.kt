package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignupActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnSignup: Button
    private lateinit var linkLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        email = findViewById(R.id.emailInput)
        password = findViewById(R.id.passwordInput)
        btnSignup = findViewById(R.id.signupButton)
        linkLogin = findViewById(R.id.loginLink)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        btnSignup.setOnClickListener {
            signupUser()
        }

        linkLogin.setOnClickListener {
            // Navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun signupUser() {
    val email = email.text.toString()
    val password = password.text.toString()

    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(this, "All fields Must be Filled", Toast.LENGTH_SHORT).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Signup successful, proceed to HomeActivity
                Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(this, "Your Email is Already Registered", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}