package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scheda, container, false)

        // Reference to Firebase Realtime Database
        val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("workouts")
        databaseReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot != null) {
                    Log.d("FirebaseData", "Data: ${dataSnapshot.value}")
                } else {
                    Log.d("FirebaseData", "No data found")
                }
            } else {
                Log.e("FirebaseData", "Error fetching data", task.exception)
            }
        }

        // Query to fetch all workouts
        val query = databaseReference

        // Configure FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<Workout>()
            .setQuery(query, Workout::class.java)
            .build()

        // Initialize the adapter
        adapter = object : FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout, parent, false)
                return WorkoutViewHolder(view)
            }

            override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int, model: Workout) {
                Log.d("SchedaFragment", "Binding workout: ${model.titolo}, ${model.descrizione}")
                holder.bind(model)
            }
        }

        // Set up RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    // ViewHolder class to bind data
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(workout: Workout) {
            Log.d("WorkoutViewHolder", "Binding workout: ${workout.titolo}, ${workout.descrizione}")
            titoloTextView.text = workout.titolo
            descrizioneTextView.text = workout.descrizione

            // Load image using Picasso
            Picasso.get()
                .load(workout.url)
                .placeholder(R.drawable.squatbilanciere) // Optional: Placeholder image
                .error(R.drawable.errore_immagine) // Optional: Error image
                .into(imageView)
        }
    }
}
