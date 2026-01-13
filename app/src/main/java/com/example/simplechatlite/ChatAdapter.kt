package com.example.simplechatlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val list: MutableList<ChatItem>,
    private val onClick: (ChatItem) -> Unit,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = list[position]

        holder.tvName.text = item.name
        holder.tvMessage.text = item.message
        holder.tvTime.text = item.time
        holder.imgProfile.setImageResource(item.profileRes)

        // 👉 Klik buka chat
        holder.itemView.setOnClickListener {
            onClick(item)
        }

        // 👉 Tekan lama untuk hapus
        holder.itemView.setOnLongClickListener {
            onLongClick(holder.adapterPosition)
            true
        }
    }

    override fun getItemCount(): Int = list.size
}
