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
import kotlin.properties.Delegates


class ProfileFragment : Fragment() {
    private lateinit var numeroIngressi : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            val nIngressi = database.getReference("users").child(userId ?: "unknown").child("ingressi")
            numeroIngressi = view.findViewById(R.id.numeroIngressi)
            numeroIngressi.text = "Numero ingressi: ${nIngressi}"
            benv.text = "Benvenuto, ${auth.currentUser?.email}"
            nIngressi.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Leggi il valore associato all'userId
                        val ingressi = snapshot.getValue(Int::class.java)
                        if (ingressi != null) {
                            numeroIngressi.text = "Numero ingressi: $ingressi"
                            Log.d("Firebase", "Ingressi: $ingressi")

                        } else {
                            Log.d("Firebase", "Ingressi non trovati")
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

}