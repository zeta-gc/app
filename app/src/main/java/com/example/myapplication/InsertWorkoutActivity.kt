package com.example.myapplication
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class InsertWorkoutActivity : AppCompatActivity() {
    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private lateinit var loading :ProgressBar
    private lateinit var scheda: Scheda
    private lateinit var workoutList: MutableList<Workout>
    private lateinit var currentUser : FirebaseUser
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_workout)
        loading = findViewById(R.id.loading)
        currentUser = FirebaseAuth.getInstance().currentUser!!
        userID = currentUser?.uid.toString()

        // Recupera la scheda passata come extra
        scheda = intent.getSerializableExtra("scheda") as Scheda
        workoutList = scheda.workoutList.toMutableList()

        loading.visibility = View.VISIBLE


        val recyclerView: RecyclerView = findViewById(R.id.cards)
        recyclerView.layoutManager = LinearLayoutManager(this)

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
                Log.d("DEBUGDBB", parent.context.toString())
                return WorkoutViewHolder(view)
            }

            override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int, model: Workout) {
                holder.bind(model)
                Log.d("DEBUGDBB", model.toString())
                val isSelected = workoutList.contains(model) ?: false
                    holder.checkBox.isChecked = isSelected

                holder.checkBox.setOnClickListener {
                    val isChecked = holder.checkBox.isChecked

                    if (isChecked) {
                        workoutList.add(model)
                    } else {
                        Log.d("DEBUG", "Rimuovo l'elemento")
                        workoutList.remove(model)
                    }
                }
            }
            override fun onDataChanged() {
                super.onDataChanged()
                loading.visibility = View.GONE
            }

        }

        recyclerView.adapter = adapter

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (scheda.workoutList != workoutList) {
                    val builder = AlertDialog.Builder(this@InsertWorkoutActivity)
                    builder.setTitle("Attenzione")
                    builder.setMessage("Vuoi salvare le modifiche?")
                    builder.setPositiveButton("Si") { _, _ ->
                        scheda.workoutList = workoutList


                        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                            .getReference("users")
                            .child(userID)
                            .child("schede")
                            .child(scheda.nome)
                            .child("workoutList")

                        databaseReference.setValue(workoutList)
                            .addOnSuccessListener {
                                Log.d("DEBUG", "Lista workout aggiornata correttamente.")
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("ERROR", "Errore durante l'aggiornamento della lista: ${e.message}")
                            }
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        adapter.stopListening()
    }

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







