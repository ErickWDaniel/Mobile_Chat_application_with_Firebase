package com.atcclass.register.UserChats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2

        fun toDateAndTimeString(time: String?): String {
            if (time == null) return ""
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date: Date = dateFormat.parse(time) as Date
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - date.time

            // convert time to string
            return when {
                diff < 60 * 1000 -> "just now"
                diff < 2 * 60 * 1000 -> "a minute ago"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
                diff < 2 * 60 * 60 * 1000 -> "an hour ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 2 * 24 * 60 * 60 * 1000 -> "yesterday"
                else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            VIEW_TYPE_SENT -> R.layout.sender
            VIEW_TYPE_RECEIVED -> R.layout.receiver
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return when (viewType) {
            VIEW_TYPE_SENT -> SentViewHolder(view)
            VIEW_TYPE_RECEIVED -> ReceiveViewHolder(view)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        if (holder is SentViewHolder) {
            holder.sentMessageTextView.text = message.message
            holder.sentDateTextView.text = toDateAndTimeString(message.timestamp)
        } else if (holder is ReceiveViewHolder) {
            holder.receiveMessageTextView.text = message.message
            holder.receiveDateTextView.text = toDateAndTimeString(message.timestamp)
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessageTextView: TextView = itemView.findViewById(R.id.senderTv)
        val sentDateTextView: TextView = itemView.findViewById(R.id.dateAndTimeTvSender)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessageTextView: TextView = itemView.findViewById(R.id.receiverTv)
        val receiveDateTextView: TextView = itemView.findViewById(R.id.dateAndTimeTvReceiver)
    }
}

