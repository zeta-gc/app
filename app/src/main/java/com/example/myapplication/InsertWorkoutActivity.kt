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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.common.reflect.TypeToken
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.squareup.picasso.Picasso

class InsertWorkoutActivity : AppCompatActivity() {
    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private var workoutList: MutableList<Workout> = mutableListOf()  // Lista dei workout associata alla scheda
    private lateinit var sharedPreferences: SharedPreferences
    private var oldWorkoutList = mutableListOf<Workout>()
    lateinit var nomescheda: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_workout)
        sharedPreferences = getSharedPreferences("SchedePrefs", Context.MODE_PRIVATE)
        Log.d("DEBUG", sharedPreferences.all.toString())
        nomescheda = intent.getStringExtra("scheda") as String
        Log.d("DEBUGSCHEDA", nomescheda)
        // Ricevi la lista di workout passata dall'intent
        workoutList = intent.getSerializableExtra("workoutList") as? MutableList<Workout> ?: mutableListOf()
        oldWorkoutList = workoutList.toMutableList()
        Log.d("DEBUGWORKOUT", workoutList.toString())



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
                Log.d("DEBUG", model.titolo)
                // Gestisci la selezione del checkBox
                holder.checkBox.isChecked = workoutList.contains(model)  // Imposta il checkBox in base alla selezione

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
                Log.d("DEBUG", workoutList.toString())
                if (oldWorkoutList != workoutList) {
                    val builder = AlertDialog.Builder(this@InsertWorkoutActivity)
                    builder.setTitle("Attenzione")
                    builder.setMessage("Vuoi salvare le modifiche?")
                    builder.setPositiveButton("Si") { _, _ ->
                        updateWorkoutList()  // Aggiorna la lista dei workout selezionati
                        finish()  // Chiudi l'attività
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

    // Funzione per aggiornare la lista dei workout
    private fun updateWorkoutList() {
        // Parse schedeList from sharedPreferences
        val gson = Gson()
        val schedeListJson = sharedPreferences.getString("schedeList", "[]") // Default to empty list
        val type = object : TypeToken<MutableList<Scheda>>() {}.type
        val schedeList: MutableList<Scheda> = gson.fromJson(schedeListJson, type)

        // Find the scheda with the matching nome and update its workoutList
        for (scheda in schedeList) {
            if (scheda.nome == nomescheda) {
                scheda.workoutList = workoutList
                break
            }
        }

        // Save the updated schedeList back to sharedPreferences
        val updatedSchedeListJson = gson.toJson(schedeList)
        sharedPreferences.edit().putString("schedeList", updatedSchedeListJson).apply()

        // Log the updated schedeList for debugging
        Log.d("DEBUGCACHE", updatedSchedeListJson)

        // Pass the updated workoutList to the result intent
        val resultIntent = Intent()
        resultIntent.putExtra("updatedWorkoutList", ArrayList(workoutList)) // Convert to ArrayList for Intent
        setResult(RESULT_OK, resultIntent)
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






