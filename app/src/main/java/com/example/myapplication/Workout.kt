package com.example.myapplication

import java.io.Serializable

data class Workout(
    val titolo: String,
    val descrizione: String,
    val url: String,
    val video: String
) : Serializable {
    constructor() : this("", "", "", "")
}