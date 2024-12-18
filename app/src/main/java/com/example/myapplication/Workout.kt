package com.example.myapplication

import java.io.Serializable

data class Workout(
    val titolo: String,
    val descrizione: String,
    val url: String
) : Serializable {
    constructor() : this("", "", "")
}