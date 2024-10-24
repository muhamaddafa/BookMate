package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

open class BaseAuthFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin()
        } else if (isUserBlocked()) {
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        // Menggunakan Intent untuk memulai LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Menutup activity saat ini agar tidak kembali ke fragment saat menekan back
    }

    private fun isUserBlocked(): Boolean {
        // Implementasi logika untuk memeriksa apakah pengguna diblokir
        return false
    }
}