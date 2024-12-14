package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
        setContentView(R.layout.reset_layout)

        val textEmail : EditText = findViewById(R.id.emailForReset)
        val btnReset : Button = findViewById(R.id.resetBtn)
        btnReset.setOnClickListener {
            val email = textEmail.text.toString()
            if (email.isEmpty())
                Toast.makeText(this,"Fornisci email", Toast.LENGTH_SHORT).show()
            else {
                ResetPassword(auth,email)
            }
        }

    }
    fun ResetPassword (auth: FirebaseAuth, mail : String) {
        auth.sendPasswordResetEmail(mail).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                val intetResetOk = Intent(this, MainActivity::class.java)
                startActivity(intetResetOk)
            }
            else {
                // If sign in fails, display a message to the user.
                Log.w("login", "Reset:failure", task.exception)
                Toast.makeText(
                    baseContext,
                    "Authentication failed. ${task.exception?.message}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

}