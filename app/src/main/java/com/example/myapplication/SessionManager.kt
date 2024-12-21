package com.example.myapplication

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SessionManager(user: String) {
    val workoutDatabaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(user).child("session")
    lateinit var currentWorkout : Workout
    lateinit var currentScheda : Scheda


    fun updateCurrentWorkout(newWorkout: Workout) {
        workoutDatabaseReference.child("currentWorkout").setValue(newWorkout)
        currentWorkout = newWorkout
    }

    fun updateCurrentScheda(newScheda: Scheda) {
        currentScheda = newScheda
        workoutDatabaseReference.child("currentScheda").setValue(newScheda)
    }

    suspend fun isUserInSession(): Boolean {
        return try {
            val snapshot = workoutDatabaseReference.get().await()
            snapshot.exists() // Returns true if the "session" node exists
        } catch (e: Exception) {
            Log.e("SessionManager", "Error checking session existence: ${e.message}")
            false
        }
    }

    fun terminateSession() {
        workoutDatabaseReference.removeValue()
    }

}