package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class InsertWorkoutActivity : AppCompatActivity() {
    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private val workoutList = mutableListOf<Workout>()
    private lateinit var sharedPreferences: SharedPreferences
    private val selectedWorkouts = mutableSetOf<String>()  // Set per memorizzare gli esercizi selezionati

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_workout)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Show an AlertDialog to ask for confirmation
                if (selectedWorkouts ==  sharedPreferences.getStringSet("selectedWorkouts", mutableSetOf())) {
                    finish()
                    return
                }
                val builder = AlertDialog.Builder(this@InsertWorkoutActivity)
                builder.setTitle("Conferma")
                builder.setMessage("Hai delle modifiche non salvate")

                // "Conferma" button
                builder.setPositiveButton("Salva modifiche") { _, _ ->
                    saveSelectedWorkouts()  // Save the selected workouts
                    finish()  // Close the activity
                }

                // "Annulla" button
                builder.setNegativeButton("Esci senza salvare") { dialog, _ ->
                    finish()
                }

                builder.show()  // Show the dialog
            }
        })

        sharedPreferences = getSharedPreferences("SelectedWorkouts", Context.MODE_PRIVATE)

        val recyclerView: RecyclerView = findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("workouts")
        val query = databaseReference

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

                // Controlla se l'esercizio è selezionato e aggiorna lo stato del CheckBox
                holder.checkBox.isChecked = selectedWorkouts.contains(model.titolo)

                holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedWorkouts.add(model.titolo)  // Aggiungi all'elenco degli esercizi selezionati
                        Toast.makeText(this@InsertWorkoutActivity, "Selezionato: ${model.titolo}", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedWorkouts.remove(model.titolo)  // Rimuovi dall'elenco degli esercizi selezionati
                        Toast.makeText(this@InsertWorkoutActivity, "Deselezionato: ${model.titolo}", Toast.LENGTH_SHORT).show()
                    }

                    // Salva gli esercizi selezionati nelle SharedPreferences
                }
            }
        }

        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()  // Inizia l'ascolto dei dati
        loadSelectedWorkouts()  // Carica gli esercizi selezionati dalla cache
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()  // Ferma l'ascolto dei dati
    }

    // Carica gli esercizi selezionati da SharedPreferences
    public fun loadSelectedWorkouts() {
        val savedWorkouts = sharedPreferences.getStringSet("selectedWorkouts", mutableSetOf())
        if (savedWorkouts != null) {
            selectedWorkouts.addAll(savedWorkouts)
        }
    }

    // Salva gli esercizi selezionati in SharedPreferences
    private fun saveSelectedWorkouts() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("selectedWorkouts", selectedWorkouts)
        editor.apply()
    }


        // Other initialization code...



    // ViewHolder per il RecyclerView
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val cardView: CardView = itemView.findViewById(R.id.cardworkout)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)  // Aggiungi il CheckBox

        fun bind(workout: Workout) {
            titoloTextView.text = workout.titolo
            descrizioneTextView.text = workout.descrizione

            Picasso.get()
                .load(workout.url)
                .placeholder(R.drawable.squatbilanciere) // Placeholder
                .error(R.drawable.errore_immagine) // Immagine di errore
                .into(imageView)
        }
    }
}
