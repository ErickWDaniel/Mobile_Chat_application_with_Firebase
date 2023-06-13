package com.atcclass.register.start.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var context: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // below Firebase line is very crucial, it's for saving data offline
//       FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        //TODO ask your Teacher why is this making the app crush
        // while its for check  database instant in phone,unless its do something else that i don't know

        FirebaseApp.initializeApp(requireContext())

        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recyclecychat)
        recyclerView.layoutManager = LinearLayoutManager(context)

        databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList = ArrayList<Users>()
                val currentUserID =
                    FirebaseAuth.getInstance().currentUser?.uid // get the current user's id
                usersList.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(Users::class.java)

                    // check if the user is not null and not the current user
                    if ((user != null) && (user.uid != currentUserID)) {
                        usersList.add(user)
                    }
                    Log.e(TAG, "DATA Toka Chat 11 ${snapshot}")
                }
                userAdapter = UserAdapter(usersList, context)
                recyclerView.adapter = userAdapter
            }

            @SuppressLint("LongLogTag")
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to read value.", error.toException())

            }
        })

        return view


    }


    companion object {
        const val TAG = "chatfragment_inaleta noma"
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onCreateOptionsMenu(menu, inflater)",
            "androidx.fragment.app.Fragment"
        )
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }
}
