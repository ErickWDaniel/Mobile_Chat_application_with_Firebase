package com.atcclass.register.start.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.atcclass.register.R
import com.atcclass.register.authetication.Login
import com.atcclass.register.databinding.ActivityMainBinding
import com.atcclass.register.profile.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
                val levels = arrayOf("1", "2", "3", "4", "5", "6", "7.1", "7.2", "8")

                val builder = AlertDialog.Builder(this)
                builder.setTitle("CREATE GROUP")

                val inputGroup = EditText(this)
                inputGroup.hint = "Group Name"
                val inputModule = EditText(this)
                inputModule.hint = "Module Name"
                val levelSpinner = Spinner(this)
                levelSpinner.adapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levels)

                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                layout.addView(inputGroup)
                layout.addView(inputModule)
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

                builder.setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }

                builder.show()

                return true
            }

            R.id.profileletu -> {

                val letchangeProfile = Intent(this, Profile::class.java)
                startActivity(letchangeProfile)
                finish()

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

                val users = mutableListOf<String>()
                snapshot.children.forEach { userSnapshot ->
                    val email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                    users.add(email)
                }

                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Add users to group")

                val userViews = mutableListOf<LinearLayout>()
                users.forEach { user ->
                    val view = LinearLayout(this@MainActivity)
                    view.orientation = LinearLayout.HORIZONTAL

                    val checkBox = CheckBox(this@MainActivity)
                    checkBox.text = user

                    view.addView(checkBox)
                    userViews.add(view)
                }

                val layout = LinearLayout(this@MainActivity)
                layout.orientation = LinearLayout.VERTICAL
                userViews.forEach { view -> layout.addView(view) }

                builder.setView(layout)

                builder.setPositiveButton("OK") { _, _ ->
                    val selectedUsers = mutableListOf<String>()
                    userViews.forEachIndexed { index, view ->
                        val checkBox = view.getChildAt(0) as CheckBox
                        if (checkBox.isChecked) {
                            selectedUsers.add(users[index])
                        }
                    }

                    val groupNode = database.child("groups").child(uid)
                    val usersNode = groupNode.child("our_members")
                    selectedUsers.forEach { user ->
                        usersNode.child(user.replace(".", ",")).setValue(true)
                    }

                    // show a success message
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