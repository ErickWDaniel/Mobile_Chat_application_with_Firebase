package com.atcclass.register.start.ui.group
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.groupChats.GroupAndUserChat
import com.atcclass.register.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class GroupAdapter(
    private val groupsList: MutableList<Groups>,
    private val context: Context
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_view_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentGroup = groupsList[position]
        holder.bind(currentGroup)

        // Set on long click listener to delete group
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteGroup(position)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

        holder.itemView.setOnClickListener {
            val groupName = currentGroup.group_name
            val moduleName = currentGroup.module_name
            if (groupName != null) {
                if (moduleName != null) {
                    fetchGroupUid(groupName, moduleName) { groupUid ->
                        val letsChat = Intent(context, GroupAndUserChat::class.java)
                        letsChat.putExtra("name", groupName)
                        letsChat.putExtra("group_uid", groupUid)
                        letsChat.putExtra("module_name", moduleName)
                        context.startActivity(letsChat)
                    }
                }
            }
        }
    }

    private fun fetchGroupUid(groupName: String, moduleName: String, callback: (String) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("groups")
        val query = databaseReference.orderByChild("group_name").equalTo(groupName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (groupSnapshot in snapshot.children) {
                    val group = groupSnapshot.getValue(Groups::class.java)
                    if (group != null && group.module_name == moduleName) {
                        val groupUid = groupSnapshot.key
                        callback(groupUid ?: "")
                        return
                    }
                }
                callback("") // If no matching group_uid found, invoke callback with an empty string
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that may occur during the database fetch
                // You can log the error or show an error message to the user
            }
        })
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }

    private fun deleteGroup(position: Int) {
        groupsList.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.groupNameTextView)
        private val level: TextView = itemView.findViewById(R.id.levelTextView)
        private val moduleName: TextView = itemView.findViewById(R.id.moduleTextView)
        private val groupImage: CircleImageView = itemView.findViewById(R.id.groupDp)

        fun bind(currentGroup: Groups) {
            groupName.text = currentGroup.group_name
            level.text = currentGroup.level
            moduleName.text = currentGroup.module_name

            // Set the first letter of the group name as the image
            val firstLetter = currentGroup.group_name?.firstOrNull()
            if (firstLetter != null) {
                //here wer set the first letter to be capital letter for the group image
                val drawable = createTextDrawable(firstLetter.uppercaseChar())
                groupImage.setImageDrawable(drawable)
            }

        }

        private fun createTextDrawable(text: Char): Drawable {
            val size = context.resources.getDimensionPixelSize(R.dimen.circle_image_size)
            val drawable = BitmapDrawable(context.resources, Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888))
            drawable.setBounds(0, 0, size, size)

            val bitmap = drawable.bitmap
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = ContextCompat.getColor(context, R.color.circle_image_background)
            canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = size.toFloat() * 0.6f
            paint.textAlign = Paint.Align.CENTER
            val xPos = canvas.width / 2f
            val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
            canvas.drawText(text.toString(), xPos, yPos, paint)

            return drawable
        }
    }

}
