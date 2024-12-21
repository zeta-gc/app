package com.example.myapplication

import android.util.Log
import com.google.common.base.Objects
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.tasks.await

class SessionManager(private val userId: String) {
    private val workoutDatabaseReference = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users").child(userId).child("session")
    private var currentWorkout: Workout? = null
    private lateinit var currentScheda: Scheda
    // This function listens for changes in the session and notifies the callback
    fun listenForSessionChanges(callback: (Boolean, DataSnapshot?) -> Unit) {
        workoutDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Trigger the callback with the snapshot if data is successfully retrieved
                    Log.d("SessionManager", "Data retrieved: ${snapshot.value}")
                    if (snapshot.child("scheda").exists()) {
                        setSessionScheda(snapshot.child("scheda").getValue(Scheda::class.java)!!)
                    }

                    callback(true, snapshot)
                } catch (e: Exception) {
                    // Handle unexpected errors in processing the snapshot
                    Log.e("SessionManager", "Error processing data: ${e.message}", e)
                    callback(false, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error message and invoke the callback with failure
                Log.e("SessionManager", "Database error: ${error.message}", error.toException())
                callback(false, null)
            }
        })
    }

    // This function checks the session once (without a listener)
    fun checkSession(callback: (Boolean) -> Unit) {
        workoutDatabaseReference.get().addOnSuccessListener { snapshot ->
            callback(snapshot.exists())
        }.addOnFailureListener {
            Log.e("SessionManager", "Error checking session existence: ${it.message}")
            callback(false)
        }
    }
    fun setSessionScheda(scheda: Scheda) {
        workoutDatabaseReference.child("scheda").setValue(scheda)
        if(scheda.workoutList.isNotEmpty()) {
            workoutDatabaseReference.child("currentWorkoutIndex").setValue(0)
            currentWorkout = scheda.workoutList[0]
            currentScheda = scheda
        }else{
        }
    }

    fun terminateSession() {
        workoutDatabaseReference.removeValue()
    }

    fun getCurrentWorkout(): Workout? {

        return currentWorkout?:null
    }
}
