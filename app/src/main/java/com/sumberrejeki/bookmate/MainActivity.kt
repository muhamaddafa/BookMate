package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigation?.setupWithNavController(navController)
        bottomNavigation.visibility = View.VISIBLE

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            // Intent untuk berpindah ke AddBookActivity
            val intent = Intent(this, AddBookActivity::class.java)
            startActivity(intent)
        }
    }
}