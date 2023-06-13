package com.atcclass.register.authetication

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.atcclass.register.start.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.atcclass.register.R
import de.hdodenhof.circleimageview.CircleImageView

class Login : AppCompatActivity() {
    private lateinit var signup: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogin: Button
    private lateinit var userEmailEtv: TextView
    private lateinit var showPasswordCheckbox: CheckBox
    private lateinit var forgotPassword: TextView
    private lateinit var userPasswordLog: TextView
    private lateinit var mediaplayeryetu: MediaPlayer
    private lateinit var atclogo: CircleImageView
    private lateinit var TitleAtc:TextView
    private lateinit var logoLayout:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // To hide the status bar, the one showing battery, network, etc.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        //Logo animation .AnimationUtils library.
        atclogo = findViewById(R.id.atclogo)
        val logoRotation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        atclogo.startAnimation(logoRotation)


        TitleAtc=findViewById(R.id.TitleAtc)
        val titleZoom=AnimationUtils.loadAnimation(this,R.anim.zoom)
        TitleAtc.startAnimation(titleZoom)


        auth = FirebaseAuth.getInstance()
//here i have attach our mp3 file from raw directory
        //the main point here is class MediaPlayer Library...there alot of functionalities in this library
        //
        mediaplayeryetu = MediaPlayer.create(this, R.raw.iphonenotf)

        // here it the step to Check if user is already signed and on tip of it the program will
        // check if the email is veriiefd...One thing to note:This is very importart for authetification,using of real
        // emails.
        //TODO the questioin below
        // My challenges are What if student use 10minutes email services and create conjunction on the server?
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // User is already signed in and email is verified, start MainActivity and finish Login activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        userEmailEtv = findViewById(R.id.editTextLogin)
        userPasswordLog = findViewById(R.id.editTextPasswordLogin)

        buttonLogin = findViewById(R.id.buttonLogin)
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox)

        signup = findViewById(R.id.textViewSignup)
        signup.setOnClickListener {
            val goToRegister = Intent(this, Register::class.java)
            startActivity(goToRegister)
            finish()
        }

        buttonLogin.setOnClickListener {
            val userEmail = userEmailEtv.text.toString().trim()
            val userPassword = userPasswordLog.text.toString().trim()


            if (userEmail.isEmpty()) {
                userEmailEtv.error = "Email is required"
                userEmailEtv.requestFocus()
                return@setOnClickListener
            }

            if (!isValidEmail(userEmail)) {
                userEmailEtv.error = "Invalid email format"
                userEmailEtv.requestFocus()
                return@setOnClickListener
            }

            if (userPassword.isEmpty()) {
                userPasswordLog.error = "Password is required"
                userPasswordLog.requestFocus()
                return@setOnClickListener
            }

            // Call login function to authenticate user
            login(userEmail, userPassword)
        }

        forgotPassword = findViewById(R.id.textViewForgot_password)
        forgotPassword.setOnClickListener {
            val gotoForgotPasswordPage = Intent(this, ForgetPassword::class.java)
            startActivity(gotoForgotPasswordPage)
        }

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userPasswordLog.transformationMethod = null
            } else {
                userPasswordLog.transformationMethod = PasswordTransformationMethod()
            }
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    logoLayout=findViewById(R.id.logoLayout)
                    val chaserLogo=AnimationUtils.loadAnimation(this,R.anim.chaser)
                    logoLayout.startAnimation(chaserLogo)
                    val currentUser = auth.currentUser
                    if (currentUser != null && currentUser.isEmailVerified) {
                        // User is ogged in and email is verified, start MainActivity and finish Login activity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        // For Music Notification hahaha
                        mediaplayeryetu.start()
                        Toast.makeText(this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // User is either not logged in or email is not verified
                        userPasswordLog.error =
                            "Authentication failed. Please check your email and password and try again."
                        userPasswordLog.requestFocus()

                        if (currentUser != null) {
                            // User is logged in but email is not verified, sign them out
                            auth.signOut()
                        }
                    }
                } else {
                    // Login failed, show error message
                    userPasswordLog.error =
                        "Authentication failed. Please check your email and password and try again."
                    userPasswordLog.requestFocus()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+")
        return email.matches(emailPattern)
    }
}
