package com.example.myapplication

data class Scheda(
    val nome: String = "",  // Nome della scheda
    var workoutList: List<Workout> = emptyList()  // Lista di workout contenuti nella scheda
)
