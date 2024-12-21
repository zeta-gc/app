package com.example.myapplication
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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