package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MyRecyclerViewAdapter(
    context: Context,
    private val mData: Array<Workout>
) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    // Inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    // Binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData[position]
        holder.myTextView.text = animal.titolo
        holder.description.text= animal.descrizione
        Picasso.get()
            .load(animal.url)
            .placeholder(R.drawable.squatbilanciere) // Immagine di placeholder
            .error(R.drawable.errore_immagine)       // Immagine di errore
            .into(holder.imageURL)
    }

    // Total number of rows
    override fun getItemCount(): Int = mData.size

    // Stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val myTextView: TextView = itemView.findViewById(R.id.workoutname)
        val description: TextView = itemView.findViewById(R.id.workoutDescription)
        val imageURL: ImageView = itemView.findViewById(R.id.workoutImage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    // Convenience method for getting data at click position
    fun getItem(id: Int): Workout = mData[id]

    // Allows click events to be caught
    fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // Parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}
