package com.example.simplechatlite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatListActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val allChatList = mutableListOf<ChatItem>()
    private val displayList = mutableListOf<ChatItem>()

    // RESULT LAUNCHER UNTUK ADD CHAT
    private val addChatLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val chatName = data.getStringExtra("name") ?: return@registerForActivityResult
                val chatMessage = data.getStringExtra("message") ?: ""

                val newChat = ChatItem(
                    name = chatName,
                    message = chatMessage,
                    time = "Baru",
                    profileRes = R.drawable._408175
                )

                allChatList.add(0, newChat)
                refreshList()

                showAddChatNotification(chatName)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val rvChat: RecyclerView = findViewById(R.id.rvChat)
        val etSearch: EditText = findViewById(R.id.etSearch)
        val fabAddChat: FloatingActionButton = findViewById(R.id.fabAddChat)

        // DATA AWAL
        allChatList.addAll(
            listOf(
                ChatItem("Andhika", "Halo apa kabar?", "11:15", R.drawable._408175),
                ChatItem("Mann", "Gas ngopi?", "10:45", R.drawable._408175),
                ChatItem("Siti", "Sudah sampai?", "09:30", R.drawable._408175),
                ChatItem("Admin", "Akun kamu aktif", "Kemarin", R.drawable._408175)
            )
        )

        displayList.addAll(allChatList)

        // INISIALISASI ADAPTER
        chatAdapter = ChatAdapter(
            list = displayList,
            onClick = { chat ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("receiverName", chat.name)
                startActivity(intent)
            },
            onLongClick = { position ->
                val removedChat = displayList[position]
                allChatList.remove(removedChat)
                refreshList()
            }
        )

        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        // FILTER SEARCH
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChat(s.toString())
            }
        })

        // ADD CHAT
        fabAddChat.setOnClickListener {
            addChatLauncher.launch(Intent(this, AddChatActivity::class.java))
        }
    }

    // REFRESH LIST UNTUK RECYCLERVIEW
    private fun refreshList() {
        displayList.clear()
        displayList.addAll(allChatList)
        chatAdapter.notifyDataSetChanged()
    }

    // FILTER CHAT SESUAI KEYWORD
    private fun filterChat(keyword: String) {
        displayList.clear()

        if (keyword.isBlank()) {
            displayList.addAll(allChatList)
        } else {
            val key = keyword.lowercase()
            displayList.addAll(
                allChatList.filter {
                    it.name.lowercase().contains(key) ||
                            it.message.lowercase().contains(key)
                }
            )
        }
        chatAdapter.notifyDataSetChanged()
    }

    // NOTIFIKASI CHAT BARU
    private fun showAddChatNotification(name: String) {
        val channelId = "chat_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Baru",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("Pesan baru")
            .setContentText("Chat dari $name")
            .setAutoCancel(true)
            .build()

        // GANTI ID NOTIF AGAR AMAN
        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}
