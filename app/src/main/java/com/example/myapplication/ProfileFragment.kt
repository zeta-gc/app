package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null){
            Log.d("Firebase", "Email non trovata")
        }
        else{
            val benv : TextView = view.findViewById(R.id.benvenuto)
            val userId = auth.currentUser?.uid
            val database = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
            val userRef = database.getReference("users").child(userId ?: "unknown")
            Log.d("userRef", userRef.toString())
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Leggi il valore associato all'userId
                        val email = snapshot.child("email").getValue(String::class.java)
                        if (email != null) {
                            benv.text = "Benvenuto, $email"
                            Log.d("Firebase", "Email: $email")

                        } else {
                            Log.d("Firebase", "Email non trovata")
                        }
                    } else {
                        Log.d("Firebase", "L'utente non esiste nel database")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore
                    Log.e("Firebase", "Errore nella lettura del database: ${error.message}")
                }
            })

        }
        val logoutBtn : FloatingActionButton = view.findViewById(R.id.logout)
        logoutBtn.setOnClickListener{
            auth.signOut()
            val tornaAlLogin  = Intent(requireContext(), MainActivity::class.java)
            startActivity(tornaAlLogin)
            requireActivity().finish()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}