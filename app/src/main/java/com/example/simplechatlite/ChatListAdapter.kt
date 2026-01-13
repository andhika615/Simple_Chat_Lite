package com.example.simplechatlite

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ChatListAdapter(
    private val context: Context,
    private val chatList: List<ChatItem>
) : BaseAdapter() {

    override fun getCount(): Int = chatList.size

    override fun getItem(position: Int): ChatItem = chatList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_chat, parent, false)

        val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)

        val item = getItem(position)

        imgProfile.setImageResource(item.profileRes)
        tvName.text = item.name
        tvMessage.text = item.message
        tvTime.text = item.time

        return view
    }
}
