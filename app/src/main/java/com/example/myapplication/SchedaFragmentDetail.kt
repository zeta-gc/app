package com.example.myapplication
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class SchedaDetailFragment : Fragment() {

    private  lateinit var nomeShedaView : TextView
    private lateinit var scheda : Scheda
    private lateinit var adapter: FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>
    private lateinit var currentUser : FirebaseUser
    private lateinit var userID: String
    private var workoutList = mutableListOf<Workout>()
    private lateinit var fab: FloatingActionButton
    // Aggiungi il launcher come variabile di classe
    private lateinit var insertWorkoutLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheda_detail, container, false)
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener(){
            val intent = Intent(requireContext(), InsertWorkoutActivity::class.java)
            intent.putExtra("scheda", scheda)
            startActivity(intent)
        }
        scheda = arguments?.getSerializable("scheda") as Scheda
        workoutList = scheda.workoutList.toMutableList()
        val recyclerView: RecyclerView = view.findViewById(R.id.cards)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        nomeShedaView = view.findViewById(R.id.nomeSchedaTextView)
        nomeShedaView.text = "SCHEDA: ${scheda.nome}"


        currentUser = FirebaseAuth.getInstance().currentUser!!
        userID = currentUser?.uid.toString()
        val databaseReference = userID?.let {
            FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(it)
                .child("schede")
                .child(scheda.nome)
                .child("workoutList")
        }

        // Ascolta i cambiamenti dei dati
        if (databaseReference != null) {
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("DEBUGDB", "Snapshot: $snapshot")
                    if (snapshot.exists()) {
                        workoutList.clear()
                        // Converte i dati in una lista di oggetti Workout
                        for (dataSnapshot in snapshot.children) {
                            val workout = dataSnapshot.getValue(Workout::class.java)
                            workout?.let {
                                workoutList.add(it)
                            }
                        }
                        scheda.workoutList = workoutList
                        // Se la lista è cambiata, aggiorna workoutList
                        Log.d("DEBUG", "Workout list: $workoutList")
                    } else {
                        Log.d("DEBUG", "Nessun workout trovato.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ERROR", "Errore durante il recupero dei dati: ${error.message}")
                }
            })
        }

        val query = databaseReference
        if (query == null) {
            Log.e("ERROR", "Nodo 'schede' non inizializzato correttamente.")
            return view
        }
        val options =
            FirebaseRecyclerOptions.Builder<Workout>()
                .setQuery(query, Workout::class.java)
                .build()

        adapter = object : FirebaseRecyclerAdapter<Workout, WorkoutViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):WorkoutViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout, parent, false)
                return WorkoutViewHolder(view)
            }

            override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int, model: Workout) {
                holder.bind(model)
                Log.d("DEBUG", model.titolo)
                // Gestisci la selezione del checkBox

            }
        }

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

    override fun onResume() {
        scheda = arguments?.getSerializable("scheda") as Scheda
        adapter.notifyDataSetChanged()
        super.onResume()
    }
    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val cardView: CardView = itemView.findViewById(R.id.cardworkout)

        fun bind(workout: Workout) {
            titoloTextView.text = workout.titolo
            descrizioneTextView.text = workout.descrizione
            cardView.setOnClickListener(){
                val videoId = "dQw4w9WgXcQ"  // Replace with your YouTube video ID
                Log.d("DEBUG", "Video ID: ${workout.video}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${workout.video}"))
                intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://com.google.android.youtube"))
                itemView.context.startActivity(intent)
            }
            Picasso.get()
                .load(workout.url)
                .placeholder(R.drawable.squatbilanciere) // Placeholder
                .error(R.drawable.errore_immagine) // Immagine di errore
                .into(imageView)
        }
    }

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


}