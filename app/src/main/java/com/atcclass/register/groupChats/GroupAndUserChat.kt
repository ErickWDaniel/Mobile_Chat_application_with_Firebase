package com.atcclass.register.groupChats

import GroupMessageAdapter
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.R
import com.atcclass.register.start.ui.chat.ChatFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class GroupAndUserChat : AppCompatActivity() {

    private lateinit var convoGroupRecycler: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: GroupMessageAdapter
    private lateinit var messageList: ArrayList<GroupMessage>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var backChatBtn: FloatingActionButton
    private lateinit var attachButton: FloatingActionButton

    private var groupRoom: String? = null
    private var senderUid: String? = null

    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_and_user_chat)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        attachButton = findViewById(R.id.attachgroupicon)
        attachButton.setOnClickListener {
            showAttachOptionsDialog()
        }
        val groupUid = intent.getStringExtra("group_uid")
        val userName = intent.getStringExtra("name")

        val toolbar = findViewById<Toolbar>(R.id.my_toolbartoo)
        toolbar.title = userName?.uppercase()

        backChatBtn = findViewById(R.id.backChatBtntoo)
        backChatBtn.setOnClickListener {
            val goBack = Intent(this, ChatFragment::class.java)
            startActivity(goBack)
        }

        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().reference

        groupRoom = groupUid // Generate a unique group room ID

        convoGroupRecycler = findViewById(R.id.groupconvosgrouprecycle)
        messageBox = findViewById(R.id.chatBoxgroup)
        sendButton = findViewById(R.id.sendMessageButtongroup)
        messageList = ArrayList()
        messageAdapter = GroupMessageAdapter(this, messageList)

        convoGroupRecycler.layoutManager = LinearLayoutManager(this)
        convoGroupRecycler.adapter = messageAdapter

        sendButton.setOnClickListener {
            val messageTxt = messageBox.text.toString().trim()

            if (messageTxt.isNotEmpty()) {
                val message = GroupMessage(senderUid!!, messageTxt)
                groupRoom?.let { groupId ->
                    val groupRef = mDbRef.child("groups").child(groupId)
                    val groupMessagesRef = groupRef.child("group_messages")
                    val newMessageRef = groupMessagesRef.push()
                    newMessageRef.setValue(message)
                        .addOnSuccessListener {
                            messageBox.text.clear()
                            convoGroupRecycler.scrollToPosition(messageAdapter.itemCount - 1)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@GroupAndUserChat,
                                "Failed to send message.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }



        groupRoom?.let { groupId ->
            val groupRef = mDbRef.child("groups").child(groupId)
            val groupMessagesRef = groupRef.child("group_messages")
            groupMessagesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(GroupMessage::class.java)
                        message?.let {
                            messageList.add(it)
                        }
                    }

                    messageAdapter.notifyDataSetChanged()
                    convoGroupRecycler.scrollToPosition(messageAdapter.itemCount - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@GroupAndUserChat,
                        "Failed to load chat.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }


    }

    private fun showAttachOptionsDialog() {
        val options = arrayOf("Image", "File")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Attach")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        checkStoragePermission()
                    }
                    1 -> {
                        openFileChooser()
                    }
                }
            }
            .show()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        // Implement the code to open the image picker here
    }

    private fun openFileChooser() {
        // Implement the code to open the file chooser here
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Unable to attach image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
