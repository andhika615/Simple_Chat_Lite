package com.example.simplechatlite

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

        // ===== VIEW =====
        val etName: EditText = findViewById(R.id.etName)
        val etMessage: EditText = findViewById(R.id.etMessage)
        val btnSave: Button = findViewById(R.id.btnAdd)

        // ===== BUTTON SAVE =====
        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(
                    this,
                    "Nama kontak tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("name", name)
                putExtra("message", message)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
