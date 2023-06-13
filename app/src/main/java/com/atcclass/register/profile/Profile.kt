package com.atcclass.register.profile


import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atcclass.register.R
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.atcclass.register.start.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class Profile : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mStorageRef: StorageReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var userNameEditText: EditText
    private lateinit var admissionNumberEditText: EditText
    private lateinit var profileImageUpdate: CircleImageView
    private lateinit var saveButton:Button
    private lateinit var changePhotoButton:Button
    private lateinit var profileback:Button
    private val pickImageRequestCode = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        mDbRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)
        mStorageRef = FirebaseStorage.getInstance().reference.child("image_url").child(currentUser.uid)

        // Initialize views
        profileImageUpdate = findViewById(R.id.profile_image_Update)
        userNameEditText = findViewById(R.id.username_edittext_update)
        admissionNumberEditText = findViewById(R.id.admission_number_text_view_update)
        saveButton = findViewById(R.id.save_button_update)
        changePhotoButton = findViewById(R.id.change_photo_button)

        // Fetch user details from database and populate views
        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val admissionNumber = snapshot.child("admission_number").value.toString()

                    // Get the URL for the most recent image
                    val photoUrl = snapshot.child("image_url").value?.toString() ?: ""

                    userNameEditText.setText(name)
                    admissionNumberEditText.setText(admissionNumber)

                    // Load profile image using Picasso
                    if (photoUrl.isNotEmpty()) {
                        Picasso.get()
                            .load(photoUrl)
                            .placeholder(R.drawable.usericon)
                            .error(R.drawable.userprofileterror)
                            .into(profileImageUpdate)
                    } else {
                        // If there is no photo URL in the database, set the default image
                        profileImageUpdate.setImageResource(R.drawable.usericon)
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
                Toast.makeText(this@Profile, "Failed to read user data", Toast.LENGTH_SHORT).show()
            }
        })
        //BAck to Memo
        profileback=findViewById(R.id.profileback)
        profileback.setOnClickListener {
            val twendeMemo=Intent(this,MainActivity::class.java)
            startActivity(twendeMemo)

        }


        // Open gallery to select profile image
        changePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequestCode)
        }

        // Save changes to user profile
        // Set on click listener for save button
        saveButton.setOnClickListener {
            // Get updated data from edit text views
            val updatedUsername = userNameEditText.text.toString().trim()
            val updatedAdmissionNumber = admissionNumberEditText.text.toString().trim()

            if (updatedUsername.isEmpty() || updatedAdmissionNumber.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else {
                // Update user profile data in databaseC
                val profileCurrentUser = mAuth.currentUser
                if (profileCurrentUser != null) {
                    val userData = hashMapOf(
                        "name" to updatedUsername,
                        "admission_number" to updatedAdmissionNumber,
                        "uid" to profileCurrentUser.uid
                    )

                    mDbRef.updateChildren(userData as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequestCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data

            // Upload image to Firebase Storage
            val imageRef = mStorageRef.child(imageUri?.lastPathSegment!!)
            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get the image URL from Firebase Storage
                val imageUrlTask = taskSnapshot.storage.downloadUrl
                imageUrlTask.addOnSuccessListener { imageUrl ->
                    // Save the image URL to the Realtime Database
                    mDbRef.child("image_url").setValue(imageUrl.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


