package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
/*
        val textUsername : EditText = findViewById(R.id.user)
        val textPassword : EditText = findViewById(R.id.pass)
        val textLogin : Button = findViewById(R.id.accessBt)

 */
        val textRegister : Button = findViewById(R.id.registerButton)
        textRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        /*
                textLogin.setOnClickListener {
                    val username = textUsername.text.toString()
                    val password = textPassword.text.toString()
                }
        */



    }
}