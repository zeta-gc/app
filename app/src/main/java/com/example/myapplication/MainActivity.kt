package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)

        val textUsername : EditText = findViewById(R.id.user)
        val textPassword : EditText = findViewById(R.id.pass)
        val textLogin : Button = findViewById(R.id.accessBt)
        val textResetPass : TextView = findViewById(R.id.resetPass)

        val textRegister : Button = findViewById(R.id.registerButton)
        textRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

                textLogin.setOnClickListener {

                    val username = textUsername.text.toString()
                    val password = textPassword.text.toString()
                    if (username.isEmpty() || password.isEmpty()){
                        Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    auth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("login", "signInWithEmail:success")
                                val user = auth.currentUser
                                Log.d("login", "sto passando")
                                val intentLogin = Intent(this, UserProfileActivity::class.java)
                                startActivity(intentLogin)

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("login", "signInWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed. ${task.exception?.message}",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                }
        textResetPass.setOnClickListener{
            val intentReset = Intent(this, ResetActivity::class.java)
            startActivity(intentReset)
        }



    }
}