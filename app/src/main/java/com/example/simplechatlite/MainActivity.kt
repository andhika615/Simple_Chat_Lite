package com.example.simplechatlite

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // 🔥 PAKSA LOGOUT SETIAP BUKA APP
        auth.signOut()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)

        // ===== LOGIN EMAIL =====
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                toast("Email & password wajib diisi")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    toast(it.message ?: "Login gagal")
                }
        }

        // ===== REGISTER =====
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.length < 6) {
                toast("Password minimal 6 karakter")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    val user = auth.currentUser ?: return@addOnSuccessListener

                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.uid)
                        .setValue(
                            mapOf(
                                "uid" to user.uid,
                                "email" to user.email,
                                "loginType" to "email"
                            )
                        )

                    auth.signOut()
                    toast("Registrasi sukses, silakan login")
                    etPassword.setText("")
                }
                .addOnFailureListener {
                    toast(it.message ?: "Register gagal")
                }
        }

        // ===== GOOGLE SIGN IN =====
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                startActivityForResult(
                    googleSignInClient.signInIntent,
                    RC_SIGN_IN
                )
            }
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (!task.isSuccessful) {
                toast("Google Sign-In gagal")
                return
            }

            val account = task.result ?: return
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val user = auth.currentUser ?: return@addOnSuccessListener

                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.uid)
                        .setValue(
                            mapOf(
                                "uid" to user.uid,
                                "email" to user.email,
                                "loginType" to "google"
                            )
                        )

                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    toast(it.message ?: "Login Google gagal")
                }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
