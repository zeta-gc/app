package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SchedaAdapter(
    private val schedeList: List<Scheda>,
    private val onItemClick: (Scheda) -> Unit
) : RecyclerView.Adapter<SchedaAdapter.SchedaViewHolder>() {

    class SchedaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeSchedaTextView: TextView = itemView.findViewById(R.id.titoloTextView)
        val cardView : CardView = itemView.findViewById(R.id.cardworkout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return SchedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchedaViewHolder, position: Int) {
        val scheda = schedeList[position]
        holder.nomeSchedaTextView.text = scheda.nome
        holder.cardView.setOnClickListener { onItemClick(scheda) }
    }

    override fun getItemCount(): Int = schedeList.size
}
