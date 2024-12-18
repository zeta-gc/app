package com.example.myapplication

import java.io.Serializable

data class Scheda(
    val nome: String = "",  // Nome della scheda
    var workoutList: List<Workout> = emptyList()  // Lista di workout contenuti nella scheda
) : Serializable {
    constructor() : this("", emptyList())
}