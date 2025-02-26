package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import the Log class
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class RegisterActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val email: EditText = findViewById(R.id.mailUser)
        val password: EditText = findViewById(R.id.passUser)
        val confPass: EditText = findViewById(R.id.passUserConf)
        val radioG: RadioGroup = findViewById(R.id.radioG)
        radioG.clearCheck()
        val btnRegistrazione: Button = findViewById(R.id.Breg)
        val auth = FirebaseAuth.getInstance()
        btnRegistrazione.setOnClickListener {
            val mail = email.text.toString()
            val pass = password.text.toString()
            val confP = confPass.text.toString()
            val genderId = radioG.checkedRadioButtonId
            if (genderId == -1) {
                Toast.makeText(this, "Seleziona un genere", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val gender = findViewById<RadioButton>(genderId).text.toString()
            Log.d("genere", "Selezionato:$gender")
            if (mail.isEmpty())
                Toast.makeText(this, "Inserisci email", Toast.LENGTH_SHORT).show()
            else if (pass.isEmpty())
                Toast.makeText(this, "Inserisci password", Toast.LENGTH_SHORT).show()
            else if (pass != confP)
                Toast.makeText(this, "Le password non coincidono", Toast.LENGTH_SHORT).show()
            else {

                registerUser(mail, pass,gender, auth) { isSuccess, errorMessage ->
                    if (isSuccess) {
                        Toast.makeText(this, "Registrazione completata", Toast.LENGTH_SHORT).show()
                        val intentLogin = Intent(this, UserProfileActivity::class.java)
                        startActivity(intentLogin)
                        finish()
                    } else {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }


            }

        }

    }

    private fun registerUser(
        email: String,
        pass: String,
        gender:String,
        auth: FirebaseAuth,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val database = FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                    val userRef = database.getReference("users").child(userId ?: "unknown")
                    auth.currentUser?.sendEmailVerification()
                    val userData = mapOf(
                        "email" to email,
                        "gender" to gender,
                        "ingressi" to 0
                    )

                    userRef.setValue(userData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                callback(true, null)
                            } else {
                                callback(false, dbTask.exception?.message ?: "Database error")
                            }
                        }

                } else {
                    val error = task.exception?.message ?: "Registrazione fallita"
                    callback(false, error)
                }
            }
    }


}









