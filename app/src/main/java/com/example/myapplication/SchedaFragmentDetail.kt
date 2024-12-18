package com.example.myapplication

import android.app.Activity.RESULT_OK
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import com.squareup.picasso.Picasso

class SchedaDetailFragment : Fragment() {

    private var nomeScheda: String? = null
    private lateinit var workoutList: ArrayList<Workout> // Lista di esercizi associati alla scheda
    private lateinit var sharedPreferences: SharedPreferences
    private val selectedWorkouts = mutableSetOf<String>() // Set per memorizzare gli esercizi selezionati

    private lateinit var adapter: WorkoutAdapter

    // Aggiungi il launcher come variabile di classe
    private lateinit var insertWorkoutLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrazione dell'attività di risultato
        insertWorkoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedWorkoutList = result.data?.getSerializableExtra("updatedWorkoutList") as? List<Workout>

                updatedWorkoutList?.let {
                    workoutList.clear()
                    workoutList.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireContext().getSharedPreferences("SelectedWorkouts", Context.MODE_PRIVATE)
        val view = inflater.inflate(R.layout.fragment_scheda_detail, container, false)

        // Ottieni la lista degli esercizi passata al fragment
        workoutList = (arguments?.getSerializable(ARG_WORKOUT_LIST) as? List<Workout> ?: emptyList()) as ArrayList<Workout>

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Imposta l'adapter per il RecyclerView
        adapter = WorkoutAdapter(workoutList)
        recyclerView.adapter = adapter

        val scanner: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        scanner.setOnClickListener {
            val intent = Intent(requireContext(), InsertWorkoutActivity::class.java)
            nomeScheda = arguments?.getString(ARG_NOME_SCHEDA)
            Log.d("DEBUG", arguments?.getString(ARG_NOME_SCHEDA).toString())
            intent.putExtra("workoutList", ArrayList(workoutList))  // Passa la lista di workout come serializzabile
            intent.putExtra("scheda", nomeScheda)  // Passa la lista di workout come serializzabile
            insertWorkoutLauncher.launch(intent)  // Usa il launcher per avviare l'attività
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("DEBUG", workoutList.toString())
        adapter.notifyDataSetChanged() // Inizia ad ascoltare i dati quando il Fragment è visibile
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

    // Adapter per il RecyclerView
    class WorkoutAdapter(private val workoutList: List<Workout>) :
        RecyclerView.Adapter<WorkoutViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout, parent, false)
            return WorkoutViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
            val workout = workoutList[position]
            holder.bind(workout)
        }

        override fun getItemCount(): Int = workoutList.size
    }

    companion object {
        private const val ARG_NOME_SCHEDA = "nomeScheda"
        private const val ARG_WORKOUT_LIST = "workoutList"

        fun newInstance(nomeScheda: String, workoutList: List<Workout>) =
            SchedaDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOME_SCHEDA, nomeScheda)
                    putSerializable(ARG_WORKOUT_LIST, ArrayList(workoutList)) // Passa la lista degli esercizi
                    Log.d("DEBUG", nomeScheda)
                }
            }
    }
}

