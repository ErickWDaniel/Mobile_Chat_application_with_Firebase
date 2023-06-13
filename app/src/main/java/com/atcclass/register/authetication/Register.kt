package com.atcclass.register.authetication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.atcclass.register.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Register : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var buttonSignUp: Button
    private lateinit var userEmailEtv: TextView
    private lateinit var userPasswordLog: TextView
    private lateinit var admissionNumber: TextView
    private lateinit var nameSignup: TextView
    private lateinit var phoneNumberSignup: TextView
    private lateinit var mDbRef: DatabaseReference
    private lateinit var loginBack: TextView
    private lateinit var showPasswordCheckbox: CheckBox
    private lateinit var uploadImage: CircleImageView
    private lateinit var currentPhotoPath: String
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //hiding action bar
        supportActionBar?.hide()
        // To hide the status bar.the one showing batter,network etc
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        mAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        storage = Firebase.storage

        loginBack = findViewById(R.id.loginBack)
        loginBack.setOnClickListener {
            val goToLogInMenu = Intent(this, Login::class.java)
            startActivity(goToLogInMenu)
        }

        nameSignup = findViewById(R.id.editTextNameSignup)
        admissionNumber = findViewById(R.id.editTextAdmissionNumberSignUp)
        userEmailEtv = findViewById(R.id.editTextEmailSignup)
        phoneNumberSignup = findViewById(R.id.editTextPhoneNumberSignup)
        userPasswordLog = findViewById(R.id.editTextPasswordSignup)
        showPasswordCheckbox = findViewById(R.id.showPasswordSignuoCheckbox)

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userPasswordLog.transformationMethod = null
            } else {
                userPasswordLog.transformationMethod = PasswordTransformationMethod()
            }
        }
        buttonSignUp = findViewById(R.id.buttonSingUp)
        buttonSignUp.setOnClickListener {
            val userName = nameSignup.text.toString().trim()
            val userAdminStr = admissionNumber.text.toString().trim()
            val userEmail = userEmailEtv.text.toString().trim()
            val userPhoneStr = phoneNumberSignup.text.toString().trim()
            val userPassword = userPasswordLog.text.toString().trim()

            if (userName.isEmpty()) {
                nameSignup.error = "Username is required."
                nameSignup.requestFocus()
            } else if (userAdminStr.isEmpty()) {
                admissionNumber.error = "Admission number is required."
                admissionNumber.requestFocus()
            } else if (!userEmail.matches(Regex("\\b[A-Za-z\\d._%+-]+@[A-Za-z\\d.-]+\\.[A-Z|a-z]{2,}\\b"))) {
                userEmailEtv.error = "Invalid email format."
                userEmailEtv.requestFocus()
            } else if (!userPhoneStr.matches(Regex("^\\d{10}$"))) {
                phoneNumberSignup.error = "Phone number must be 10 digits"
                phoneNumberSignup.requestFocus()
            } else if (userPassword.isEmpty()) {
                userPasswordLog.error = "Password is required."
                userPasswordLog.requestFocus()
            } else {
                val userAdmin = userAdminStr.toLong()
                val userPhone = userPhoneStr.toInt()
                imageUrl?.let { it1 ->
                    signUp(
                        userName,
                        userAdmin,
                        userEmail,
                        userPhone,
                        userPassword,
                        it1
                    )
                }

            }
        }
        uploadImage = findViewById(R.id.profile_image)

        //To upload image
        uploadImage.setOnClickListener {

            myUploadImage()
        }

        // Create a file to store the image
        val photoFile = createImageFile()

        // Get the file path of the image
        if (photoFile != null) {
            currentPhotoPath = photoFile.absolutePath
        }

        // ...

    }

    private fun signUp(
        username: String,
        userAdmin: Long,
        userEmail: String,
        userPhone: Int,
        userPass: String,
        imageUrl: String // Added parameter for image URL
    ) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = mAuth.currentUser
                    currentUser?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {

                            // Email verification sent successfully
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                            currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        val userData = hashMapOf(
                                            "name" to username,
                                            "admission_number" to userAdmin,
                                            "phone_number" to userPhone,
                                            "email" to userEmail,
                                            "uid" to currentUser.uid,
                                            "image_url" to imageUrl // Save image URL in user data
                                        )

                                        mDbRef = FirebaseDatabase.getInstance().reference
                                        mDbRef.child("users").child(currentUser.uid).setValue(userData)
                                            .addOnSuccessListener {
                                                nameSignup.text = ""
                                                admissionNumber.text = ""
                                                userEmailEtv.text = ""
                                                phoneNumberSignup.text = ""
                                                userPasswordLog.text = ""
                                                Toast.makeText(
                                                    this,
                                                    "ACCOUNT CREATED",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Send user to login activity after successful registration
                                                startActivity(Intent(this, Login::class.java))
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                userPasswordLog.error =
                                                    "Registration failed:\n Error: ${e.message}"
                                                userPasswordLog.requestFocus()
                                            }

                                    } else {
                                        userPasswordLog.error = "Registration failed:\n Error"
                                        userPasswordLog.requestFocus()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to send verification email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed:\nError:${task.exception?.message}".uppercase(),
                        Toast.LENGTH_SHORT
                    ).show()
                    nameSignup.text = ""
                    admissionNumber.text = ""
                    userEmailEtv.text = ""
                    phoneNumberSignup.text = ""
                    userPasswordLog.text = ""
                }
            }
    }




    // Define a constant to identify the request to pick an image
    private val requestRequestCapture = 1
    private val requestImagePick = 2

    // Create a method to launch the image picker
    private fun myUploadImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("⛱ Add Photo 🏖")
        builder.setItems(options) { dialog, item ->
            when (item) {
                0 -> {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    takePicture()
                }
                1 -> {
                    pickPhoto()
                }
                2 -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }




    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        pictureIntent.resolveActivity(packageManager)?.also {
            try {
                createImageFile()?.also { photoFile ->
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "${packageName}.images",
                        photoFile
                    )
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(pictureIntent, requestRequestCapture)

                    // Upload the image to Firebase Storage//TODO CHECK LATER THE PERFORMANCE
                    val storageRef = storage.reference.child("image_url/${photoURI?.lastPathSegment}.jpg")
                    val imageRef = storageRef.child("images/${photoFile.name}")
                    val uploadTask = imageRef.putFile(photoURI)
                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        // Image upload successful
                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                           Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        // Image upload failed
                        Toast.makeText(this, "Image upload failed: $exception", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (ex: IOException) {
                Toast.makeText(this, "Error creating image file Error:$ex", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, "jpg", storageDir)
    }

    private fun pickPhoto() {
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).also { pickPhotoIntent ->
            pickPhotoIntent.type = "image/*"
            startActivityForResult(pickPhotoIntent, requestImagePick)
        }
    }



    @Deprecated("Deprecated in Java")
    private var imageUrl: String? = null // Class-level variable to store the image URL

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImagePick && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            val imageUri = data.data
            if (imageUri != null) {
                // Upload the image to Firebase Storage
                val storageRef = storage.reference.child("images/${imageUri.lastPathSegment}")
                val uploadTask = storageRef.putFile(imageUri)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Image upload successful, get the download URL
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Get the download URL as a string
                        imageUrl = uri.toString()

                        // Use the image URL in the same activity
                        if (imageUrl != null) {
                            // Do something with the image URL
                            // For example, display it in an ImageView
                            Picasso.get()
                                .load(imageUrl)
                                .resize(50, 50)
                                .centerCrop()
                                .into(uploadImage)
                        }

                        Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        // Failed to get the image download URL, handle the error
                        Log.e(TAG, "Error getting image URL: ${e.message}", e)
                    }
                }.addOnFailureListener { e ->
                    // Image upload failed, handle the error
                    Log.e(TAG, "Error uploading image: ${e.message}", e)
                }
            }
        }
    }


}
