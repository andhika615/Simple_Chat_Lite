package com.example.simplechatlite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.*
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var dbRef: DatabaseReference

    private var typingContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val btnProfile = findViewById<Button>(R.id.btnProfile)

        chatLayout = findViewById(R.id.chatLayout)
        scrollView = findViewById(R.id.scrollView)

        dbRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child("global")

        loadMessages()

        // ===== SEND USER =====
        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isEmpty()) return@setOnClickListener

            val msgId = dbRef.push().key ?: return@setOnClickListener
            dbRef.child(msgId).setValue(
                mapOf(
                    "text" to userText,
                    "sender" to "user",
                    "time" to getTime()
                )
            )

            etMessage.setText("")

            // ===== BOT =====
            showTyping()
            Handler(Looper.getMainLooper()).postDelayed({
                hideTyping()
                saveBotToFirebase(botReply(userText))
            }, 1200)
        }

        btnClear.setOnClickListener {
            dbRef.removeValue()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // ===== LOAD CHAT =====
    private fun loadMessages() {
        dbRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                val text = snapshot.child("text").value.toString()
                val sender = snapshot.child("sender").value.toString()
                val time = snapshot.child("time").value.toString()

                addBubble(text, time, sender == "user", snapshot.key!!)
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                val id = snapshot.key
                val newText = snapshot.child("text").value.toString()

                for (i in 0 until chatLayout.childCount) {
                    val view = chatLayout.getChildAt(i)
                    if (view.tag == id) {
                        val container = view as LinearLayout
                        val msg = container.getChildAt(0) as TextView
                        msg.text = newText
                        break
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.key
                for (i in 0 until chatLayout.childCount) {
                    val view = chatLayout.getChildAt(i)
                    if (view.tag == id) {
                        chatLayout.removeView(view)
                        break
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ===== SAVE BOT =====
    private fun saveBotToFirebase(text: String) {
        val msgId = dbRef.push().key ?: return
        dbRef.child(msgId).setValue(
            mapOf(
                "text" to text,
                "sender" to "bot",
                "time" to getTime()
            )
        )
        showBotNotification(text)
    }

    // ===== CHAT BUBBLE =====
    private fun addBubble(text: String, timeText: String, isMe: Boolean, msgId: String) {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = msgId
        }

        val msg = TextView(this).apply {
            this.text = text
            setPadding(32, 20, 32, 10)
            setTextColor(getColor(android.R.color.white))
            setBackgroundResource(
                if (isMe) R.drawable.bubble_right else R.drawable.bubble_left
            )
        }

        val time = TextView(this).apply {
            this.text = timeText
            textSize = 10f
            setTextColor(getColor(android.R.color.white))
            setPadding(32, 0, 32, 8)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (isMe) Gravity.END else Gravity.START
            setMargins(16, 8, 16, 8)
        }

        if (isMe) {
            msg.setOnClickListener {
                showEditDialog(msgId, msg)
            }

            msg.setOnLongClickListener {
                showMessageOptions(msgId, msg)
                true
            }
        }

        container.layoutParams = params
        container.addView(msg)
        container.addView(time)
        chatLayout.addView(container)
    }

    // ===== MESSAGE OPTIONS =====
    private fun showMessageOptions(msgId: String, msgView: TextView) {
        val options = arrayOf("Edit Pesan", "Hapus Pesan")

        AlertDialog.Builder(this)
            .setTitle("Opsi Pesan")
            .setItems(options) { _, which ->
                if (which == 0) showEditDialog(msgId, msgView)
                else dbRef.child(msgId).removeValue()
            }
            .show()
    }

    private fun showEditDialog(msgId: String, msgView: TextView) {
        val et = EditText(this)
        et.setText(msgView.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Pesan")
            .setView(et)
            .setPositiveButton("Simpan") { _, _ ->
                dbRef.child(msgId).child("text").setValue(et.text.toString())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ===== BOT TYPING =====
    private fun showTyping() {
        if (typingContainer != null) return

        val text = TextView(this).apply {
            text = "Bot sedang mengetik..."
            setPadding(32, 20, 32, 20)
            setTextColor(getColor(android.R.color.white))
            setBackgroundResource(R.drawable.bubble_left)
        }

        typingContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
                setMargins(16, 8, 16, 8)
            }
            addView(text)
        }

        chatLayout.addView(typingContainer)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun hideTyping() {
        typingContainer?.let { chatLayout.removeView(it) }
        typingContainer = null
    }

    // ===== BOT LOGIC (NON RANDOM) =====
    private fun botReply(userMsg: String): String {
        val msg = userMsg.lowercase(Locale.getDefault())

        return when {
            msg.contains("halo") || msg.contains("hai") ->
                "Halo 👋 Ada yang bisa saya bantu?"

            msg.contains("apa kabar") ->
                "Saya baik 😊 Terima kasih sudah bertanya."

            msg.contains("jam") || msg.contains("waktu") ->
                "Sekarang pukul ${getTime()}"

            msg.contains("nama") ->
                "Saya SimpleChat Bot 🤖"

            msg.contains("terima kasih") || msg.contains("makasih") ->
                "Sama-sama 🙏"

            msg.contains("bye") || msg.contains("dadah") ->
                "Sampai jumpa 👋"

            else ->
                "Saya mengerti. Bisa dijelaskan lebih detail?"
        }
    }

    // ===== UTIL =====
    private fun getTime(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun showBotNotification(msg: String) {
        val channelId = "chat_channel"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Chat",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Chat Bot")
            .setContentText(msg)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notif)
    }
}
