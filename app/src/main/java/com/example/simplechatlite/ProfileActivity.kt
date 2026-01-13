package com.example.simplechatlite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvEmail.text = user.email

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
