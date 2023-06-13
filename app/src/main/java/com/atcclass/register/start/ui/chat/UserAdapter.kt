package com.atcclass.register.start.ui.chat

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.R
import com.atcclass.register.UserChats.One2OneChat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val usersList: MutableList<Users>,
    private val context: Context
) : RecyclerView.Adapter<UserAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentUser = usersList[position]
        holder.userNameTextView.text = currentUser.name
        holder.emailTextView.text = currentUser.email

        val mDbRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid!!)

        mDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.child("image_url").value?.toString()

                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.usericon)
                        .error(R.drawable.userprofileterror)
                        .into(holder.userDP)
                } else {
                    Picasso.get()
                        .load(R.drawable.usericon)
                        .into(holder.userDP)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading image URL", error.toException())
                // Handle the error here (e.g., display a default image)
                Picasso.get()
                    .load(R.drawable.usericon)
                    .into(holder.userDP)
            }
        })

        holder.itemView.setOnClickListener {
            val letsChat = Intent(context, One2OneChat::class.java)
            letsChat.putExtra("name", currentUser.name)
            letsChat.putExtra("uid", currentUser.uid)
            context.startActivity(letsChat)
        }

        //On long hold you can delete your own list on your screen
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this Chat?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteChat(position)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            true
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    private fun deleteChat(position: Int) {
        usersList.removeAt(position)
        notifyItemRemoved(position)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.ModuleTextView)
        val userDP: CircleImageView = itemView.findViewById(R.id.userDpChat)
    }
}
