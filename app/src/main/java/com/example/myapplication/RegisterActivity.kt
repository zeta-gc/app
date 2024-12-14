package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

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
            val radioGBtn: RadioButton = findViewById(genderId)

            if (mail.isEmpty())
                Toast.makeText(this, "Inserisci email", Toast.LENGTH_SHORT).show()
            else if (pass.isEmpty())
                Toast.makeText(this, "Inserisci password", Toast.LENGTH_SHORT).show()
            else if (pass != confP)
                Toast.makeText(this, "Le password non coincidono", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "fin qui ci siamo", Toast.LENGTH_SHORT).show()
                registerUser(mail, pass,auth)

            }

        }

    }

    private fun registerUser(email: String, pass: String, auth: FirebaseAuth) {
        Toast.makeText(this, "fin qui ci siamo", Toast.LENGTH_SHORT).show()
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
            Toast.makeText(this, "fin qui ci siamo ${it.isSuccessful}", Toast.LENGTH_SHORT).show()
            if (it.isSuccessful){
                Toast.makeText(this, "reg compl",Toast.LENGTH_SHORT).show()
            }
        }


        /*
                    val intent = Intent(this, UserProfileActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    */

    }

}









