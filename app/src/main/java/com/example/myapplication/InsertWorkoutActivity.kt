package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class InsertWorkoutActivity : AppCompatActivity() {
    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private var oldWorkoutList = mutableListOf<Workout>()
    private lateinit var scheda: Scheda
    private lateinit var workoutList: MutableList<Workout>
    private lateinit var currentUser : FirebaseUser
    private lateinit var userID: String
    lateinit var nomescheda: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_workout)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        userID = currentUser?.uid.toString()

        // Recupera la scheda passata come extra
        scheda = intent.getSerializableExtra("scheda") as Scheda
        workoutList = scheda.workoutList.toMutableList()

        // Inizializza workoutList con la lista esistente di workout

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userID)
            .child("schede")
            .child(scheda.nome)
            .child("workoutList")


        val recyclerView: RecyclerView = findViewById(R.id.cards)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Ottieni i workout disponibili da un altro nodo
        val workoutDatabaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("workouts")

        val query = workoutDatabaseReference

        val options = FirebaseRecyclerOptions.Builder<Workout>()
            .setQuery(query, Workout::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout_all, parent, false)
                return WorkoutViewHolder(view)
            }

            override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int, model: Workout) {
                holder.bind(model)
                Log.d("DEBUGDBB", model.toString())

                // Imposta il checkBox in base alla selezione
                holder.checkBox.isChecked = workoutList.contains(model)

                holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        workoutList.add(model)  // Aggiungi l'esercizio alla lista dei workout
                    } else {
                        workoutList.remove(model)  // Rimuovi l'esercizio dalla lista dei workout
                    }
                }
            }
        }

        recyclerView.adapter = adapter

        // Gestione del tasto "Indietro" (back)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (scheda.workoutList != workoutList) {
                    val builder = AlertDialog.Builder(this@InsertWorkoutActivity)
                    builder.setTitle("Attenzione")
                    builder.setMessage("Vuoi salvare le modifiche?")
                    builder.setPositiveButton("Si") { _, _ ->
                        scheda.workoutList = workoutList

                        // Aggiorna la lista nel database
                        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                            .getReference("users")
                            .child(userID)
                            .child("schede")
                            .child(scheda.nome)
                            .child("workoutList")

                        databaseReference.setValue(workoutList)
                            .addOnSuccessListener {
                                Log.d("DEBUG", "Lista workout aggiornata correttamente.")
                                finish()  // Chiudi l'attività
                            }
                            .addOnFailureListener { e ->
                                Log.e("ERROR", "Errore durante l'aggiornamento della lista: ${e.message}")
                            }
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()  // Chiudi il dialogo senza fare nulla
                    }
                    builder.show()
                } else {
                    finish()  // Se non ci sono workout selezionati, esci direttamente
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)  // Aggiungi il callback per il tasto "Indietro"
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()  // Inizia l'ascolto dei dati
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()  // Ferma l'ascolto dei dati
    }

    // ViewHolder per il RecyclerView
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val cardView: CardView = itemView.findViewById(R.id.cardworkout)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(workout: Workout) {
            titoloTextView.text = workout.titolo
            descrizioneTextView.text = workout.descrizione

            Picasso.get()
                .load(workout.url)
                .placeholder(R.drawable.squatbilanciere)
                .error(R.drawable.errore_immagine)
                .into(imageView)
        }
    }
}







