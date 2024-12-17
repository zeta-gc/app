package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

/*class SchedaFragment : Fragment(), MyRecyclerViewAdapter.ItemClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: MyRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sample data to populate the RecyclerView
        val lista: Array<Workout> = arrayOf(
            Workout("Allenamento Cardio", "ziatta", "https://media.licdn.com/dms/image/v2/C5603AQG8UdMc2Fli_w/profile-displayphoto-shrink_200_200/profile-displayphoto-shrink_200_200/0/1516307193470?e=2147483647&v=beta&t=sbJ-41oYO3RTB3HREZ86DyRs4vMGiMcC5_57Oy2pQHY"),
            Workout("Forza Muscolare", "ziatttta", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTYl0Ef_noaQm7mhcVROMZaIRbnznkJloEcoA&s"),

        )

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyRecyclerViewAdapter(requireContext(), lista)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scheda, container, false)
    }

    override fun onItemClick(view: View, position: Int) {
        val item = adapter.getItem(position)
        Toast.makeText(requireContext(), "CLICCATO $item", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SchedaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}*/
class SchedaFragment : Fragment() {

    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private val workoutList = mutableListOf<Workout>()  // Lista per memorizzare i dati
    private lateinit var sharedPreferences: SharedPreferences
    private val selectedWorkouts = mutableSetOf<String>()  // Set per memorizzare gli esercizi selezionati
    private lateinit var noSelectedWorkoutsText: TextView  // Riferimento al TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireContext().getSharedPreferences("SelectedWorkouts", Context.MODE_PRIVATE)
        val view = inflater.inflate(R.layout.fragment_scheda, container, false)
        noSelectedWorkoutsText = view.findViewById(R.id.noSelectedWorkoutsText)
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("workouts")
        val query = databaseReference

        val options = FirebaseRecyclerOptions.Builder<Workout>()
            .setQuery(query, Workout::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout, parent, false)
                return WorkoutViewHolder(view)
            }

            override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int, model: Workout) {
                // Mostra solo gli esercizi selezionati
                if (selectedWorkouts.contains(model.titolo)) {
                    holder.bind(model)
                    holder.cardView.visibility = View.VISIBLE  // Mostra la card
                } else {
                    holder.cardView.visibility = View.GONE  // Nascondi la card se non è selezionata
                }

                // Gestione del click sugli elementi
                holder.cardView.setOnClickListener {
                    Toast.makeText(requireContext(), "CLICK ${model.titolo}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scanner: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        scanner.setOnClickListener {
            val intent = Intent(requireContext(), InsertWorkoutActivity::class.java)
            startActivity(intent)
        }
    }
    public fun loadSelectedWorkouts() {
        val savedWorkouts = sharedPreferences.getStringSet("selectedWorkouts", mutableSetOf())
        if (savedWorkouts != null) {
            selectedWorkouts.clear()
            selectedWorkouts.addAll(savedWorkouts)
        }

        // Verifica se la lista è vuota e mostra il testo appropriato
        if (selectedWorkouts.isEmpty()) {
            noSelectedWorkoutsText.visibility = View.VISIBLE  // Mostra il messaggio
        } else {
            noSelectedWorkoutsText.visibility = View.GONE  // Nascondi il messaggio
        }
    }
    override fun onStart() {
        super.onStart()
        if (adapter != null)
            adapter.startListening()
       loadSelectedWorkouts()  // Cari
        adapter.notifyDataSetChanged()  // Inizia ad ascoltare i dati quando il Fragment è visibile
    }

    override fun onResume() {
        super.onResume()
        loadSelectedWorkouts()  // Cari
        adapter.startListening()
        adapter.notifyDataSetChanged()  // Inizia ad ascoltare i dati quando il Fragment è visibile
    }

    override fun onPause() {
        super.onPause()
        if (adapter != null)
            adapter.stopListening()  // Ferma l'ascolto quando il Fragment non è visibile
    }

    // Metodo per caricare i dati da Firebase
    private fun loadDataFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("workouts")
        databaseReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null) {
                    workoutList.clear()  // Pulisci la lista prima di aggiungere nuovi dati
                    for (snapshot in dataSnapshot.children) {
                        val workout = snapshot.getValue(Workout::class.java)
                        if (workout != null) {
                            workoutList.add(workout)
                        }
                    }
                    adapter.notifyDataSetChanged()  // Notifica all'adapter che i dati sono cambiati
                }
            } else {
                Log.e("FirebaseData", "Error fetching data", task.exception)
            }
        }
    }

    // ViewHolder per il RecyclerView
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val cardView: CardView = itemView.findViewById(R.id.cardworkout)

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

