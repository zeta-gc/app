package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.InsertWorkoutActivity.WorkoutViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso

<<<<<<< Updated upstream


=======
>>>>>>> Stashed changes
class SchedaFragment : Fragment() {
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private val schedeList = mutableListOf<Scheda>()  // Lista locale delle schede
//    private lateinit var adapter: SchedaAdapter  // Adapter per il RecyclerView
//    private  lateinit var noschedetext : TextView
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_scheda, container, false)
//
//        // Inizializza noschedetext prima di usarlo
//        noschedetext = view.findViewById(R.id.noSelectedWorkoutsText)
//
//        // Inizializza SharedPreferences
//        sharedPreferences = requireContext().getSharedPreferences("SchedePrefs", Context.MODE_PRIVATE)
//
//        // Carica le schede salvate
//        loadSchede()
//
//        // Imposta il RecyclerView
//        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        adapter = SchedaAdapter(schedeList) { scheda ->
//            // Naviga al SchedaDetailFragment subito dopo aver creato la scheda
//            val detailFragment = SchedaDetailFragment.newInstance(
//                scheda.nome, ArrayList(scheda.workoutList)
//            )
//            requireActivity().supportFragmentManager.beginTransaction()
//                .replace(R.id.frameLayout, detailFragment)
//                .addToBackStack(null)
//                .commit()
//        }
//        recyclerView.adapter = adapter
//
//        // Floating Action Button per aggiungere una nuova scheda
//        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
//        fab.setOnClickListener {
//            showAddSchedaDialog()
//        }
//
//        return view
//    }
//
//    // Mostra un dialog per aggiungere una nuova scheda
//    private fun showAddSchedaDialog() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Nuova Scheda")
//
//        val input = EditText(requireContext())
//        input.hint = "Nome della scheda"
//        builder.setView(input)
//
//        builder.setPositiveButton("Aggiungi") { _, _ ->
//            val schedaNome = input.text.toString().trim()
//            if (schedaNome.isNotEmpty()) {
//                val nuovaScheda = Scheda(nome = schedaNome)
//                schedeList.add(nuovaScheda)
//                saveSchede()  // Salva la lista aggiornata
//                adapter.notifyDataSetChanged()
//
//                // Naviga al SchedaDetailFragment subito dopo aver creato la scheda
//                val detailFragment = SchedaDetailFragment.newInstance(
//                    nuovaScheda.nome, ArrayList(nuovaScheda.workoutList)
//                )
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.frameLayout, detailFragment)
//                    .addToBackStack(null)
//                    .commit()
//            } else {
//                Toast.makeText(requireContext(), "Il nome non può essere vuoto", Toast.LENGTH_SHORT).show()
//            }
//        }
//        builder.setNegativeButton("Annulla", null)
//        builder.show()
//    }
//
//    // Carica la lista delle schede da SharedPreferences
//    private fun loadSchede() {
//        val gson = Gson()
//        val json = sharedPreferences.getString("schedeList", null)
//        val type = object : TypeToken<List<Scheda>>() {}.type
//        val loadedList: List<Scheda> = gson.fromJson(json, type) ?: emptyList()
//        schedeList.clear()
//        schedeList.addAll(loadedList)
//        if (schedeList.isEmpty()) {
//            noschedetext.visibility = View.VISIBLE
//        } else {
//            noschedetext.visibility = View.GONE
//        }
//    }
//
//    // Salva la lista delle schede in SharedPreferences
//    private fun saveSchede() {
//        val gson = Gson()
//        val json = gson.toJson(schedeList)
//        sharedPreferences.edit().putString("schedeList", json).apply()
//    }

    private  lateinit var noschedetext : TextView
    private lateinit var adapter: FirebaseRecyclerAdapter<Scheda, SchedaViewHolder>
    private lateinit var currentUser : FirebaseUser
    private lateinit var userID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheda, container, false)
        noschedetext = view.findViewById(R.id.noSelectedWorkoutsText)
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showAddSchedaDialog()
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        currentUser = FirebaseAuth.getInstance().currentUser!!
        userID = currentUser?.uid.toString()
        val databaseReference = userID?.let {
            FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(it)
                .child("schede")
        } // Nodo contenente le schede dell'utente

// Verifica e inizializza il nodo "schede" se non esiste
        databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DEBUG", "Snapshot: ${snapshot.value}") // Log dell'intero snapshot
                if (!snapshot.exists()) {
                    // Nodo "schede" non esiste, inizializzalo
                    val defaultSchede = mapOf(
                         "0" to Scheda(
                            nome = "Scheda Esempio",
                            workoutList = mutableListOf(Workout("","",""))
                        )
                    )
                    databaseReference.setValue(defaultSchede)
                        .addOnSuccessListener {
                            Log.d("DEBUG", "Nodo 'schede' inizializzato con una scheda di esempio.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ERROR", "Errore durante l'inizializzazione del nodo 'schede': ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", "Errore durante la verifica del nodo 'schede': ${error.message}")
            }
        })

        val query = databaseReference
        if (query == null) {
            Log.e("ERROR", "Nodo 'schede' non inizializzato correttamente.")
            return view
        }
        val options =
            FirebaseRecyclerOptions.Builder<Scheda>()
                .setQuery(query, Scheda::class.java)
                .build()


        adapter = object : FirebaseRecyclerAdapter<Scheda, SchedaViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedaViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_workout, parent, false)
                return SchedaViewHolder(view)
            }

            override fun onBindViewHolder(holder: SchedaViewHolder, position: Int, model: Scheda) {
                Log.d("DEBUG", "Scheda trovata: Nome = ${model.nome}, WorkoutList = ${model.workoutList}")
                holder.bind(model)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                if (itemCount == 0) {
                    noschedetext.visibility = View.VISIBLE
                } else {
                    noschedetext.visibility = View.GONE
                }
            }

        }
        recyclerView.adapter = adapter

        return view
    }

    //    // Mostra un dialog per aggiungere una nuova scheda
    private fun showAddSchedaDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nuova Scheda")

        val input = EditText(requireContext())
        input.hint = "Nome della scheda"
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { _, _ ->
            val schedaNome = input.text.toString().trim()
            if (schedaNome.isNotEmpty()) {
                val nuovaScheda = Scheda(nome = schedaNome, workoutList = emptyList())
                val databaseReference = userID?.let {
                    FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("users")
                        .child(it)
                        .child("schede")
                }
                databaseReference?.child(nuovaScheda.nome)?.setValue(nuovaScheda)
                    ?.addOnSuccessListener {
                        Log.d("DEBUG", "Scheda aggiunta con successo.")
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("ERROR", "Errore durante l'aggiunta della scheda: ${e.message}")
                    }
                adapter.notifyDataSetChanged()

            } else {
                Toast.makeText(requireContext(), "Il nome non può essere vuoto", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Annulla", null)
        builder.show()
    }
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
class SchedaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titoloTextView: TextView = itemView.findViewById(R.id.titoloTextView)
    private val descrizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
    private val imageView: ImageView = itemView.findViewById(R.id.imageView)
    val cardView: CardView = itemView.findViewById(R.id.cardworkout)

    fun bind(scheda: Scheda) {
        Log.d("DEBUG", scheda.nome)
        titoloTextView.text = scheda.nome


    }
}

