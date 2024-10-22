package com.sumberrejeki.bookmate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.Toolbar

class ForgotpasswordActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var resetButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Tombol kembali (back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        emailInput = findViewById(R.id.emailInput)
        resetButton = findViewById(R.id.resetButton)
        auth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please Input your Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
    .addOnCompleteListener { resetTask ->
        if (resetTask.isSuccessful) {
            Toast.makeText(this, "Reset Email Sent", Toast.LENGTH_SHORT).show()
            finish() //Balik Lagi Ke Login Bro
        } else {
            Toast.makeText(this, "Error: ${resetTask.exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
