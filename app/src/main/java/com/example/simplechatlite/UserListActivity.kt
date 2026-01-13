package com.example.simplechatlite

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val userList = ArrayList<String>()
    private val uidList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        listView = findViewById(R.id.listUsers)

        val currentUid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("users")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                uidList.clear()

                for (userSnap in snapshot.children) {
                    val uid = userSnap.child("uid").value.toString()
                    val email = userSnap.child("email").value.toString()

                    // jangan tampilkan diri sendiri
                    if (uid != currentUid) {
                        userList.add(email)
                        uidList.add(uid)
                    }
                }

                val adapter = ArrayAdapter(
                    this@UserListActivity,
                    android.R.layout.simple_list_item_1,
                    userList
                )
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverUid", uidList[position])
            startActivity(intent)
        }
    }
}
