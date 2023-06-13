package com.atcclass.register.authetication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.atcclass.register.R
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var resetPasswordButton: Button
    private lateinit var emailEditText: TextView
    private lateinit var goToResetBackLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        supportActionBar?.hide()

        // To hide the status bar.the one showing batter,network etc
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        resetPasswordButton = findViewById(R.id.reset_password_button)
        emailEditText = findViewById(R.id.email_edit_text)
        goToResetBackLogin = findViewById(R.id.resetbacklogin)
        goToResetBackLogin.setOnClickListener {
            val goToLoginPage = Intent(this, Login::class.java)
            startActivity(goToLoginPage)
        }
        mAuth = FirebaseAuth.getInstance()

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if the email exists before sending a password reset email

            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // Email does not exist
                        AlertDialog.Builder(this)
                            .setTitle("Email not found")
                            .setMessage("The email you entered does not exist.")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        // Email exists, send password reset email
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { resetTask ->
                            if (resetTask.isSuccessful) {
                                // Password reset email sent successfully
                                // Show a message to the user
                                Toast.makeText(
                                    this,
                                    "Password reset email sent",
                                    Toast.LENGTH_LONG
                                ).show()
                                val twendeLogin = Intent(this, Login::class.java)
                                startActivity(twendeLogin)
                                finish()
                            } else {
                                // Password reset email sending failed
                                // Show an error message to the user
                                Toast.makeText(
                                    this,
                                    resetTask.exception?.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                else {
                    // Error fetching sign-in methods
                    Toast.makeText(
                        this,
                        "An error occurred while processing your request.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}