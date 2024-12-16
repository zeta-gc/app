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
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserProfileActivity : AppCompatActivity() {
    private val richiestaPermesso = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted ->
        if (isGranted){
            apriCamera()
        }
        else {
            Toast.makeText(this,"Ho bisogno dei permessi per accedere alla fotocamera", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
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
    public fun verificaPermessi(contesto : android.content.Context){
        if (ContextCompat.checkSelfPermission(contesto,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            apriCamera()
        }
        else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(contesto,"Permesso di accesso alla fotocamera richiesto",Toast.LENGTH_SHORT).show()
        }
        else {
            richiestaPermesso.launch(android.Manifest.permission.CAMERA)
        }
    }
    private fun apriCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scansiona il QR code")
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        scanL.launch(options)
    }

    private val scanL = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this, "Scansione annullata", Toast.LENGTH_SHORT).show()
        } else {
            val scannedUrl = result.contents

            // Effettua la chiamata all'API
            inviaRichiestaAPI(scannedUrl)
        }
    }

    private suspend fun ottieniToken(): String? = suspendCoroutine { continuation ->
        val user = FirebaseAuth.getInstance().currentUser

        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                Log.d("qrscanner", "TOKEN! $token")
                continuation.resume(token) // Restituisce il token
            } else {
                Log.d("qrscanner", "Errore nel recupero del token")
                continuation.resume(null) // Restituisce null in caso di errore
            }
        }
    }
    private fun inviaRichiestaAPI(url: String) {


        // Ottieni il token (puoi recuperarlo da Firebase o da una variabile salvata)
        lifecycleScope.launch {
            val token = ottieniToken()
            if (token == null) {
                Toast.makeText(this@UserProfileActivity, "Impossibile recuperare il token", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val client = okhttp3.OkHttpClient()
        Log.d("qrscanner", "TOKEN $token")
        // Costruisci la richiesta con il token nell'header
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token") // Aggiungi il token all'header
            .build()

        // Esegui la richiesta in un thread separato
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserProfileActivity, "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.message?.let { Log.d("qrscanner", it) }
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            when(response.code){
                                403 ->   Toast.makeText(this@UserProfileActivity, "NON HAI INGRESSI DISPONIBILI", Toast.LENGTH_SHORT).show()
                                404 ->  Toast.makeText(this@UserProfileActivity, "Errore API: UTENTE NON REGISTRATO", Toast.LENGTH_SHORT).show()
                                else->{
                                    Toast.makeText(this@UserProfileActivity, "Errore API: ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Log.d("qrscanner", "$response.code")
                        }
                    } else {
                        val responseData = response.body?.string()
                        val apiResponse = Gson().fromJson(responseData, ApiResponse::class.java)
                        runOnUiThread {
                            Toast.makeText(this@UserProfileActivity, "Risposta APa: ${apiResponse.message}", Toast.LENGTH_LONG).show()
                            Log.d("qrscanner", "$responseData")
                        }
                    }
                }
            }
        })
        }
    }



    companion object {
        fun verificaPermessi(homeFragment: android.content.Context) {
            verificaPermessi(homeFragment)
        }
    }

}