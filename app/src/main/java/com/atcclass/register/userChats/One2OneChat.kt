package com.atcclass.register.userChats

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


class One2OneChat : AppCompatActivity() {
    private lateinit var convoOneOnOneRecycle: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var backChatBtn: FloatingActionButton
    private lateinit var attach_button: FloatingActionButton

    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one2_one_chat)
        supportActionBar?.hide()
        // To hide the status bar.the one showing batter,network etc'
        //This help with the screen size!Chekc Now and later
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        var selectedImageUri: Uri? = null

        //for attach files
        attach_button = findViewById(R.id.attach_send)
        var PICK_IMAGE_REQUEST = 0
        attach_button.setOnClickListener {
            //TODO make sure you can finish the upload and download of the files
            val builder = AlertDialog.Builder(this)
            // Set the dialog title and message
            builder.setTitle("Choose an option")
                .setMessage("What would you like to do?")

            // Set the positive button and its OnClickListener
            builder.setPositiveButton("Upload file") { dialog, which ->
                // Create an Intent to pick an image from the phone's storage
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }

            // Set the negative button and its OnClickListener
            builder.setNegativeButton("View file") { dialog, which ->
                // Get a reference to the Firebase Storage instance
                val storage = FirebaseStorage.getInstance()

                // Create a new dialog to display the list of files
                val fileListDialog = Dialog(this)
                fileListDialog.setContentView(R.layout.file_list_dialog)

                // Get a reference to the ListView in the dialog
                val listView = fileListDialog.findViewById<ListView>(R.id.file_list_view)

                // Create an empty list to hold the file names
                val fileList = mutableListOf<String>()

                // Get a reference to the Firebase Storage location where the files are stored
                val storageRef = storage.reference.child("uploads")

                // List all the files in the Firebase Storage location
                storageRef.listAll()
                    .addOnSuccessListener { listResult ->
                        // Add the names of the files to the list
                        for (item in listResult.items) {
                            fileList.add(item.name)
                        }

                        // Create an adapter for the ListView
                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_multiple_choice,
                            fileList
                        )

                        // Set the adapter for the ListView
                        listView.adapter = adapter

                        // Show the dialog
                        fileListDialog.show()

                        // Set a listener for the positive button in the dialog
                        fileListDialog.findViewById<Button>(R.id.dialog_ok_button)
                            .setOnClickListener {
                                // Get the list of checked items in the ListView
                                val checkedItems = listView.checkedItemPositions

                                // Download the selected files to the phone storage
                                for (i in 0 until checkedItems.size()) {
                                    val position = checkedItems.keyAt(i)
                                    if (checkedItems.valueAt(i)) {
                                        // Get the name of the selected file
                                        val fileName = fileList[position]

                                        // Get a reference to the selected file in Firebase Storage
                                        val fileRef = storageRef.child(fileName)

                                        // Get a reference to the Download Manager service
                                        val downloadManager =
                                            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                                        // Create a Download Manager request for the selected file
                                        val request =
                                            DownloadManager.Request(Uri.parse(fileRef.toString()))

                                        // Set the title and description of the Download Manager request
                                        request.setTitle(fileName)
                                        request.setDescription("Downloading file")

                                        // Set the destination directory and file name for the downloaded file
                                        request.setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            fileName
                                        )

                                        // Enqueue the Download Manager request
                                        downloadManager.enqueue(request)
                                    }
                                }

                                // Dismiss the dialog
                                fileListDialog.dismiss()
                            }
                    }
                    .addOnFailureListener { exception ->
                        // Handle any errors that occurred while listing the files
                        Toast.makeText(
                            this,
                            "Error listing files: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }


            val dialog = builder.create()
            dialog.show()


        }

        val userName = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        toolbar.title = userName?.uppercase()
        backChatBtn = findViewById(R.id.backChatBtn)
        backChatBtn.setOnClickListener {
            val goback = Intent(this, ChatFragment::class.java)
            startActivity(goback)

        }

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().reference

        receiverRoom = senderUid + receiverUid
        senderRoom = receiverUid + senderUid

        convoOneOnOneRecycle = findViewById(R.id.convosone2oneprecycle)
        messageBox = findViewById(R.id.chatBox)
        sendButton = findViewById(R.id.sendMessageButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        convoOneOnOneRecycle.layoutManager = LinearLayoutManager(this)
        convoOneOnOneRecycle.adapter = messageAdapter

        // Logic for adding data to recycle
        mDbRef.child("chats").child(senderRoom!!).child("message")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    convoOneOnOneRecycle.scrollToPosition(messageList.size - 1)
                }


                override fun onCancelled(error: DatabaseError) {
                    // TODO baadaye checki kumalizia all onCancelled issues
                }

            })

        // Let add message to the database
        sendButton.setOnClickListener {
            if (isNetworkAvailable()) {
                val message = messageBox.text.toString()
                val messageObject = Message(message, senderUid)
                // Get the current date and time
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss:a")
                // Format the date and time
                val timestamp = currentDateTime.format(formatter)
                messageObject.dateNTime = timestamp

                // Save the sender's message to the sender's chat room
                mDbRef.child("chats").child(senderRoom!!).child("message").push()
                    .setValue(messageObject).addOnSuccessListener {

                        // Save the sender's message to the receiver's chat room
                        mDbRef.child("chats").child(receiverRoom!!).child("message").push()
                            .setValue(messageObject).addOnSuccessListener {

                                // Create a notification for the receiver
                                val notificationRef =
                                    receiverUid?.let { it1 ->
                                        mDbRef.child("notifications").child(
                                            it1
                                        ).push()
                                    }
                                notificationRef?.child("type")?.setValue("message")
                                notificationRef?.child("from")?.setValue(senderUid)
                                notificationRef?.child("timestamp")?.setValue(ServerValue.TIMESTAMP)
                            }
                    }
                messageBox.text = null

            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
        // This code below is for notification if it happens I get error check this line
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetwork
        return (activeNetworkInfo != null)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var PICK_FILE_REQUEST = 100
        // Check if the result is from picking a file
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // Get the selected file URI
            val selectedFileUri = data.data!!

            // Get the file extension
            val fileExtension = contentResolver.getType(selectedFileUri)
                ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }

            // Check if the file is a picture
            if (fileExtension in listOf("jpg", "jpeg", "png", "bmp", "gif")) {
                // Upload the picture to Firebase storage
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("pictures/${selectedFileUri.lastPathSegment}")
                storageRef.putFile(selectedFileUri)
                    .addOnSuccessListener {
                        // File uploaded successfully
                        Toast.makeText(this, "Picture uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Error uploading file
                        Toast.makeText(this, "Error uploading picture", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Upload the document to Firebase storage
                val storageRef =
                    FirebaseStorage.getInstance().reference.child("documents/${selectedFileUri.lastPathSegment}")
                storageRef.putFile(selectedFileUri)
                    .addOnSuccessListener {
                        // File uploaded successfully
                        Toast.makeText(this, "Document uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Error uploading file
                        Toast.makeText(this, "Error uploading document", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    //TODO REMEMBER BELOW ITS NOT YET IMPLEMENTED
    private fun uploadFileToFirebase() {
        // Get a reference to the Firebase Storage instance
        val storage = FirebaseStorage.getInstance()
        val selectedImageUri: Uri? = null
        // Get a reference to the storage location where the file will be uploaded
        val storageRef = storage.reference.child("uploads/${UUID.randomUUID()}")

        // Upload the file to Firebase Storage
        if (selectedImageUri != null) {
            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the uploaded file
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save the sender ID, photo URL, and date of sending to the Realtime Database
                        val senderId = FirebaseAuth.getInstance().currentUser?.uid
                        val photoUrl = uri.toString()
                        val date = Calendar.getInstance().timeInMillis
                        val database = FirebaseDatabase.getInstance().reference
                        val key = database.child("uploads").push().key
                        val childUpdates = hashMapOf<String, Any>(
                            "/uploads/$key" to mapOf(
                                "senderId" to senderId,
                                "photoUrl" to photoUrl,
                                "date" to date
                            )
                        )
                        database.updateChildren(childUpdates as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "File uploaded to Firebase!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error uploading file to Realtime Database: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error uploading file to Firebase Storage: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


}


