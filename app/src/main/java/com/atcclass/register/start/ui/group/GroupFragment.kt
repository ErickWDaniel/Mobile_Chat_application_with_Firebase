package com.atcclass.register.start.ui.group

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atcclass.register.R
import com.atcclass.register.start.ui.chat.ChatFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupFragment : Fragment() {

    private lateinit var groupRecycler: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var groupListener: ValueEventListener
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
        // Initialize Firebase in the current context
        FirebaseApp.initializeApp(requireContext())

        val view = inflater.inflate(R.layout.fragment_group, container, false)
        groupRecycler = view.findViewById(R.id.recycleGroup)
        groupRecycler.layoutManager = LinearLayoutManager(requireContext())

        databaseRef = FirebaseDatabase.getInstance().getReference("groups")

        groupListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groupList = ArrayList<Groups>()
                val currentUserID =
                    FirebaseAuth.getInstance().currentUser?.uid // get the current user's id
                groupList.clear()
                for (data in snapshot.children) {
                    val group = data.getValue(Groups::class.java)

                    // check if the user is not null and not the current user
                    if ((group != null) && (group.uid != currentUserID)) {
                        groupList.add(group)
                    }
                    Log.e(TAG, "DATA Toka group new")
                }
                groupAdapter = GroupAdapter(groupList, context)
                groupRecycler.adapter = groupAdapter
            }

            @SuppressLint("LongLogTag")
            override fun onCancelled(error: DatabaseError) {
                Log.d(ChatFragment.TAG, "Failed to read value.", error.toException())

            }
        }
        databaseRef.addValueEventListener(groupListener)
        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        // remove the database listener
        databaseRef.removeEventListener(groupListener)
    }

    companion object {
        const val TAG = "GroupFragment"
    }
}
