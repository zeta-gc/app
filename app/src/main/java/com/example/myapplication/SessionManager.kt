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
    public var currentScheda: Scheda? = null
    private var currentWorkoutIndex: Int = 0
    // This function listens for changes in the session and notifies the callback
    fun listenForSessionChanges(callback: (Boolean, DataSnapshot?) -> Unit) {
        workoutDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // Trigger the callback with the snapshot if data is successfully retrieved
                    Log.d("SessionManager", "Data retrieved: ${snapshot.value}")
                    if (!snapshot.exists()) {
                        callback(false, null)
                        return
                    }
                    currentWorkoutIndex = snapshot.child("currentWorkoutIndex").getValue(Int::class.java) ?: 0
                    if (snapshot.child("scheda").exists() && currentScheda != snapshot.child("scheda").getValue(Scheda::class.java)) {
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
            currentWorkout = scheda.workoutList[currentWorkoutIndex?:0]
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

    fun skipWorkout() {
        if (currentScheda == null || currentWorkout == null) {
            Log.e("SessionManager", "Current scheda or workout is null")
            return
        }
        val currentWorkoutIndex = currentScheda!!.workoutList.indexOf(currentWorkout)
        if (currentWorkoutIndex < currentScheda!!.workoutList.size - 1) {
            Log.d("SessionManager", "Skipping workout")
            currentWorkout = currentScheda!!.workoutList[currentWorkoutIndex + 1]
            workoutDatabaseReference.child("currentWorkoutIndex").setValue(currentWorkoutIndex + 1)
            Log.d("SessionManager", "Current workout index: ${currentWorkoutIndex + 1}")
        }
    }
}
