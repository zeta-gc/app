package com.example.myapplication

data class Workout(
    val titolo: String = "",  // Default values
    val descrizione: String = "",
    val url: String = ""
) {
    // No-argument constructor is automatically provided by Kotlin data classes
    // The default values ensure Firebase can instantiate the object
}