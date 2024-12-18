package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.google.common.reflect.TypeToken
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.squareup.picasso.Picasso



class SchedaFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val schedeList = mutableListOf<Scheda>()  // Lista locale delle schede
    private lateinit var adapter: SchedaAdapter  // Adapter per il RecyclerView
    private  lateinit var noschedetext : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheda, container, false)

        // Inizializza noschedetext prima di usarlo
        noschedetext = view.findViewById(R.id.noSelectedWorkoutsText)

        // Inizializza SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("SchedePrefs", Context.MODE_PRIVATE)

        // Carica le schede salvate
        loadSchede()

        // Imposta il RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SchedaAdapter(schedeList) { scheda ->
            // Naviga al SchedaDetailFragment subito dopo aver creato la scheda
            val detailFragment = SchedaDetailFragment.newInstance(
                scheda.nome, ArrayList(scheda.workoutList)
            )
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, detailFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        // Floating Action Button per aggiungere una nuova scheda
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showAddSchedaDialog()
        }

        return view
    }

    // Mostra un dialog per aggiungere una nuova scheda
    private fun showAddSchedaDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nuova Scheda")

        val input = EditText(requireContext())
        input.hint = "Nome della scheda"
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { _, _ ->
            val schedaNome = input.text.toString().trim()
            if (schedaNome.isNotEmpty()) {
                val nuovaScheda = Scheda(nome = schedaNome)
                schedeList.add(nuovaScheda)
                saveSchede()  // Salva la lista aggiornata
                adapter.notifyDataSetChanged()

                // Naviga al SchedaDetailFragment subito dopo aver creato la scheda
                val detailFragment = SchedaDetailFragment.newInstance(
                    nuovaScheda.nome, ArrayList(nuovaScheda.workoutList)
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "Il nome non può essere vuoto", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Annulla", null)
        builder.show()
    }

    // Carica la lista delle schede da SharedPreferences
    private fun loadSchede() {
        val gson = Gson()
        val json = sharedPreferences.getString("schedeList", null)
        val type = object : TypeToken<List<Scheda>>() {}.type
        val loadedList: List<Scheda> = gson.fromJson(json, type) ?: emptyList()
        schedeList.clear()
        schedeList.addAll(loadedList)
        if (schedeList.isEmpty()) {
            noschedetext.visibility = View.VISIBLE
        } else {
            noschedetext.visibility = View.GONE
        }
    }

    // Salva la lista delle schede in SharedPreferences
    private fun saveSchede() {
        val gson = Gson()
        val json = gson.toJson(schedeList)
        sharedPreferences.edit().putString("schedeList", json).apply()
    }
}
