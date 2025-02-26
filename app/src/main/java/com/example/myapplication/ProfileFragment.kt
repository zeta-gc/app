package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates


class ProfileFragment : Fragment() {
    private lateinit var numeroIngressi: TextView
    private lateinit var setIngressiButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Log.d("Firebase", "Email non trovata")
        } else {
            val benv: TextView = view.findViewById(R.id.benvenuto)
            val userId = auth.currentUser?.uid
            val database = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
            val nIngressi = database.getReference("users").child(userId ?: "unknown").child("ingressi")

            numeroIngressi = view.findViewById(R.id.numeroIngressi)


            var ingressi = 0
            nIngressi.get().addOnSuccessListener { snapshot ->
                ingressi = snapshot.getValue(Int::class.java) ?: 0
                numeroIngressi.text = "Numero ingressi: $ingressi"
                Log.d("Firebase", "Initial ingressi value: $ingressi")
            }

            benv.text = "Benvenuto, ${auth.currentUser?.email}"

            setIngressiButton = view.findViewById(R.id.setIngressi)
            setIngressiButton.setOnClickListener {
                ingressi?.let { FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users").child(userId ?: "unknown").child("ingressi").setValue(it+10)
                    ingressi += 10
                }


            }

            nIngressi.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Firebase", "Snapshot exists: ${snapshot.exists()}")
                    if (snapshot.exists()) {
                        val ingressi = snapshot.getValue(Int::class.java)
                        if (ingressi != null) {
                            numeroIngressi.text = "Numero ingressi: $ingressi"
                            Log.d("Firebase", "Ingressi updated: $ingressi")
                        } else {
                            Log.d("Firebase", "Ingressi non trovati")
                        }
                    } else {
                        Log.d("Firebase", "L'utente non esiste nel database")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Errore nella lettura del database: ${error.message}")
                }
            })
        }

        val logoutBtn: FloatingActionButton = view.findViewById(R.id.logout)
        logoutBtn.setOnClickListener {
            auth.signOut()
            val tornaAlLogin = Intent(requireContext(), MainActivity::class.java)
            startActivity(tornaAlLogin)
            requireActivity().finish()
        }
    }
}