package com.example.myapplication
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_user_profile)
        val bottomView : BottomNavigationView = findViewById(R.id.bottomNavigationView)
        cambiaSchermata(HomeFragment())
        bottomView.setOnItemSelectedListener{ item ->
            when(item.itemId){
                R.id.homeA ->{
                    cambiaSchermata(HomeFragment())
                }
                R.id.scheda ->{
                    cambiaSchermata(SchedaFragment())
                }
                R.id.profilo ->{
                    cambiaSchermata(ProfileFragment())
                }
            }
            true
        }


    }
    fun cambiaSchermata (layout: Fragment){
        val fm : FragmentManager = supportFragmentManager
        val transaction : FragmentTransaction = fm.beginTransaction()
        transaction.replace(R.id.frameLayout,layout)
        transaction.commit()
    }









}