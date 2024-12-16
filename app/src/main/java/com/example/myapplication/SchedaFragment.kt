package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SchedaFragment : Fragment(), MyRecyclerViewAdapter.ItemClickListener {

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
        val animalNames = arrayListOf("Horse", "Cow", "Camel", "Sheep", "Goat", "EDOARDO", "MATTIA")

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.rvAnimals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyRecyclerViewAdapter(requireContext(), animalNames)
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
}
