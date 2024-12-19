package com.example.myapplication
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class SchedaFragment : Fragment() {
    private  lateinit var noschedetext : TextView
    private lateinit var loading: ProgressBar
    private lateinit var adapter: FirebaseRecyclerAdapter<Scheda, SchedaViewHolder>
    private lateinit var currentUser : FirebaseUser
    private lateinit var userID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheda, container, false)
        noschedetext = view.findViewById(R.id.noSelectedWorkoutsText)
        noschedetext.visibility = View.GONE
        loading = view.findViewById(R.id.loading)
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showAddSchedaDialog()
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.cards)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        currentUser = FirebaseAuth.getInstance().currentUser!!
        userID = currentUser?.uid.toString()
        val databaseReference = userID?.let {
            FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(it)
                .child("schede")
        }


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
                    .inflate(R.layout.item_scheda, parent, false)
                return SchedaViewHolder(view)
            }

            override fun onBindViewHolder(holder: SchedaViewHolder, position: Int, model: Scheda) {
                Log.d("DEBUG", "Scheda trovata: Nome = ${model.nome}, WorkoutList = ${model.workoutList}")
                holder.cardView.setOnClickListener({
                    val fragment = SchedaDetailFragment()

                    val bundle = Bundle()
                    bundle.putSerializable("scheda", model)  // Esempio di dato da passare

                    fragment.arguments = bundle

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)  // Facoltativo, per permettere il back
                        .commit()
                })
                holder.bind(model)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                loading.visibility = View.GONE
                if (itemCount == 0) {
                    noschedetext.visibility = View.VISIBLE
                } else {
                    noschedetext.visibility = View.GONE
                }
            }

        }
        recyclerView.adapter = adapter
        loading.visibility = View.VISIBLE
        return view
    }

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
    private val descizioneTextView: TextView = itemView.findViewById(R.id.descrizioneTextView)
    private val deleteSchedaButton : FloatingActionButton = itemView.findViewById(R.id.deleteSchedaButton)
    val cardView: CardView = itemView.findViewById(R.id.cardworkout)

    fun bind(scheda: Scheda) {
        Log.d("DEBUG", scheda.nome)
        titoloTextView.text = scheda.nome
        descizioneTextView.text = "Numero esercizi: ${scheda.workoutList.size.toString()}"
        deleteSchedaButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this.itemView.context)
            builder.setTitle("Attenzione")
            builder.setMessage("Vuoi eliminare la scheda?")
            builder.setPositiveButton("Si") { _, _ ->
                val databaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("schede")
                    .child(scheda.nome)
                databaseReference.removeValue()
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Scheda rimossa con successo.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ERROR", "Errore durante la rimozione della scheda: ${e.message}")
                    }
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()  // Chiudi il dialogo senza fare nulla
            }
            builder.show()

        }
    }
}

