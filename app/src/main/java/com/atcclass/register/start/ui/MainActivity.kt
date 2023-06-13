package com.atcclass.register.start.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.PointerIcon
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.atcclass.register.R
import com.atcclass.register.admin.Admin
import com.atcclass.register.authetication.Login
import com.atcclass.register.databinding.ActivityMainBinding
import com.atcclass.register.profile.Profile
import com.atcclass.register.students.Sms
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //The logout menu supportActionBAr
        supportActionBar?.show()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_mainactivity)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chats, R.id.navigation_group, R.id.navigation_memo
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                // log the user out and navigate to the login screen
                FirebaseAuth.getInstance().signOut()

                // Navigate the user to the login page
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return true
            }

            R.id.createGroup -> {


                val builder = AlertDialog.Builder(this)
                builder.setTitle("CREATE GROUP").setIcon(R.drawable.group_icon_group_alert)
                val inputGroup = EditText(this)
                inputGroup.hint = "Type Group Name"
                val inputModule = EditText(this)
                inputModule.hint = "Type Module Code"
                val labelForLevel=TextView(this)
                labelForLevel.text=" LEVEL"
                labelForLevel.textSize= 18F

                val levels = arrayOf("1", "2", "3", "4", "5", "6", "7.1", "7.2", "8")
                val levelSpinner = Spinner(this)



                levelSpinner.adapter =ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)

                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                layout.addView(inputGroup)
                layout.addView(inputModule)
                layout.addView(labelForLevel)
                layout.addView(levelSpinner)

                builder.setView(layout)

                builder.setPositiveButton("OK") { _, _ ->
                    val groupName = inputGroup.text.toString()
                    val moduleName = inputModule.text.toString()

                    val selectedLevel = levels[levelSpinner.selectedItemPosition]

                    // Save data to Firebase Realtime Database
                    val database = Firebase.database.reference
                    val groupNode = database.child("groups").push()

                    groupNode.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                groupNode.child("module_name").setValue(moduleName)
                            }

                            groupNode.child("level").setValue(selectedLevel)
                            groupNode.child("group_name").setValue(groupName)
                            //Let take those value for the group

                            // pass the group's uid to the user list dialog
                            showUserListDialog(groupNode.key!!, groupName)
                        }

                        @SuppressLint("LongLogTag")
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Database error: $error")
                        }
                    })
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                builder.show()

                return true
            }

            R.id.Profile -> {

                val letchangeProfile = Intent(this, Profile::class.java)
                startActivity(letchangeProfile)
                finish()

                return true
            }

            R.id.sms->{
                val goToSms=Intent(this, Sms::class.java)
                startActivity(goToSms)
                finish()
                return true
            }

            R.id.adminPage -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Admin Login").setIcon(R.drawable.admini_icon)
                val inputEmail = EditText(this)
                inputEmail.hint = "Email"
                val inputPassword = EditText(this)
                inputPassword.hint = "Password"
                inputPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                layout.addView(inputEmail)
                layout.addView(inputPassword)

                builder.setView(layout)

                builder.setPositiveButton("Login") { _, _ ->
                    val email = inputEmail.text.toString()
                    val password = inputPassword.text.toString()

                    val adminRef = FirebaseDatabase.getInstance().reference.child("admins")
                    val adminQuery = adminRef.orderByChild("email").equalTo(email)
                    adminQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (adminSnapshot in snapshot.children) {
                                    val admin = adminSnapshot.value as? Map<*, *>
                                    val adminPassword = admin?.get("password") as? String
                                    if (adminPassword == password) {
                                        // Admin found and password is correct
                                        // Start Admin activity
                                        val intent = Intent(this@MainActivity, Admin::class.java)
                                        startActivity(intent)
                                        return
                                    }
                                }
                            }

                            Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Database error: $error")
                        }
                    })
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                builder.show()

                return true
            }



            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showUserListDialog(uid: String, groupName: String) {
        val database = Firebase.database.reference
        val usersNode = database.child("users")

        usersNode.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    return
                }

                val users = mutableListOf<User>()  // Create a list of User objects to store UID, email, and name
                snapshot.children.forEach { userSnapshot ->
                    val uid = userSnapshot.key  // Get the user UID
                    val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                    val user = uid?.let { User(it, email, name) }
                    if (user != null) {
                        users.add(user)
                    }
                }

                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Select Users to Add to Group")

                val userViews = mutableListOf<LinearLayout>()
                users.forEach { user ->
                    val view = LinearLayout(this@MainActivity)
                    view.orientation = LinearLayout.HORIZONTAL

                    val checkBox = CheckBox(this@MainActivity)
                    checkBox.text = user.email  // Display email in the checkbox label

                    view.addView(checkBox)
                    userViews.add(view)
                }

                val layout = LinearLayout(this@MainActivity)
                layout.orientation = LinearLayout.VERTICAL
                userViews.forEach { view -> layout.addView(view) }

                builder.setView(layout)

                builder.setPositiveButton("OK") { _, _ ->
                    val selectedUsers = mutableListOf<User>()
                    userViews.forEachIndexed { index, view ->
                        val checkBox = view.getChildAt(0) as CheckBox
                        if (checkBox.isChecked) {
                            selectedUsers.add(users[index])
                        }
                    }

                    val groupNode = database.child("groups").child(uid)
                    val usersNodeyetu = groupNode.child("our_members")
                    selectedUsers.forEach { user ->
                        val userDetails = HashMap<String, Any>()
                        userDetails["uid"] = user.uid
                        userDetails["email"] = user.email
                        userDetails["name"] = user.name
                        usersNodeyetu.child(user.uid).setValue(userDetails)
                    }

                    // Show a success message
                    Toast.makeText(
                        this@MainActivity,
                        "Users added to $groupName",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

                builder.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: $error")
            }
        })
    }


}
data class User(val uid: String, val email: String, val name: String)
