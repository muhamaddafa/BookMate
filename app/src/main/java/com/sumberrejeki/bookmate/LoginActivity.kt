package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var linkForgotPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var linkSignup: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.emailInput)
        password = findViewById(R.id.passwordInput)
        linkForgotPassword = findViewById(R.id.forgotPasswordLink)
        btnLogin = findViewById(R.id.loginButton)
        linkSignup = findViewById(R.id.signupLink)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        linkForgotPassword = findViewById(R.id.forgotPasswordLink)

        linkForgotPassword.setOnClickListener {
            // Arahkan ke halaman reset password
            val intent = Intent(this, ForgotpasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        linkSignup.setOnClickListener {
            // Navigate to SignupActivity
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = email.text.toString()
        val password = password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful, proceed to HomeActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}