package com.atcclass.register.authetication


import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.atcclass.register.start.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.atcclass.register.R
import androidx.appcompat.app.AlertDialog
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
    private lateinit var atclogo:CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // To hide the status bar.the one showing batter,network etc
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        //Logo animation
        atclogo=findViewById(R.id.atclogo)
        val logoRotation:Animation=AnimationUtils.loadAnimation(this,com.atcclass.register.R.anim.logo_animation)
        atclogo.startAnimation(logoRotation)

        auth = FirebaseAuth.getInstance()

        mediaplayeryetu=MediaPlayer.create(this,R.raw.iphonenotf)

        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Users_Group is already signed in, start MainActivity and finish Login activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        userEmailEtv = findViewById(R.id.editTextLogin)
        userPasswordLog = findViewById(R.id.editTextPasswordLogin)

        buttonLogin = findViewById(R.id.buttonLogin)
        showPasswordCheckbox = findViewById(R.id.showPasswordCheckbox)


        signup = findViewById(R.id.textViewSignup)
        signup.setOnClickListener {
            val backsignup = Intent(this, Register::class.java)
            startActivity(backsignup)
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
                userEmailEtv.error = "Invalid emformat"
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
        auth.currentUser?.let { currentUser ->
            if (currentUser.isEmailVerified) {
                // User is already verified, call signInWithEmailAndPassword() to log them in
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login success, start MainActivity and finish Login activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            //For Music Notification hahaha
                            mediaplayeryetu.start()
                            Toast.makeText(this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            // Login failed, show error message
                            userPasswordLog.error =
                                "Authentication failed. Please check your email and password and try again."
                            userPasswordLog.requestFocus()
                        }
                    }
            } else {
                // User's email has not been verified, show alert dialog
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Your email address has not been verified yet. Please check your email and click on the verification link.")
                    .setPositiveButton("Resend verification email") { _, _ ->
                        // Resend verification email
                        currentUser.sendEmailVerification()
                            .addOnCompleteListener { resendTask ->
                                if (resendTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Verification email sent successfully. Please check your inbox and spam folder.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to send verification email. Please try again later.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Cancel login and stay on login screen
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        } ?: run {
            // No user is currently signed in, call signInWithEmailAndPassword() to log them in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login success, start MainActivity and finish Login activity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        //For Music Notification hahaha
                        mediaplayeryetu.start()
                        Toast.makeText(this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // Login failed, show error message
                        userPasswordLog.error =
                            "Authentication failed. Please check your email and password and try again."
                        userPasswordLog.requestFocus()
                    }
                }
        }
    }




    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+")
        return email.matches(emailPattern)
    }

}
