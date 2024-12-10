package com.sumberrejeki.bookmate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ShelvesPageActivity : AppCompatActivity() {

    // Daftar untuk menyimpan FrameLayout yang memiliki CheckBox yang tercentang
    private val selectedBooks = mutableListOf<FrameLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shelves_page)

        // Ambil referensi ke GridLayout
        val gridLayout: GridLayout = findViewById(R.id.grid_layout)

        // Ambil referensi ke tombol "Delete Book"
        val deleteButton: View = findViewById(R.id.delete_button)  // Pastikan Anda memiliki tombol dengan ID delete_button

        // Iterasi setiap child di GridLayout dan tambahkan listener pada CheckBox
        for (i in 0 until gridLayout.childCount) {
            val itemView = gridLayout.getChildAt(i) as FrameLayout // Setiap item adalah FrameLayout

            // Cari CheckBox di dalam FrameLayout
            val checkBox = itemView.getChildAt(0) as CheckBox

            // Jika CheckBox belum memiliki ID, berikan ID unik
            if (checkBox.id == View.NO_ID) {
                val uniqueId = View.generateViewId()
                checkBox.id = uniqueId
            }

            // Set listener untuk CheckBox
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Tambahkan item ke dalam daftar jika CheckBox dicentang
                    selectedBooks.add(itemView)
                } else {
                    // Hapus item dari daftar jika CheckBox tidak dicentang
                    selectedBooks.remove(itemView)
                }
            }
        }

        // Set listener untuk tombol Delete Book
        deleteButton.setOnClickListener {
            // Hapus item yang tercentang
            if (selectedBooks.isNotEmpty()) {
                for (itemView in selectedBooks) {
                    gridLayout.removeView(itemView)
                }
                // Clear daftar setelah menghapus item
                selectedBooks.clear()
                Toast.makeText(this, "Books deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No books selected for deletion", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
