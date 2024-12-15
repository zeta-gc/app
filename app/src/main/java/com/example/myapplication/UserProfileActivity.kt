package com.example.myapplication
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.core.Context
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

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

        val scanner : FloatingActionButton = findViewById(R.id.floatingActionButton)
        scanner.setOnClickListener{
            verificaPermessi(this)
        }
    }
    fun cambiaSchermata (layout: Fragment){
        val fm : FragmentManager = supportFragmentManager
        val transaction : FragmentTransaction = fm.beginTransaction()
        transaction.replace(R.id.frameLayout,layout)
        transaction.commit()
    }
    private fun verificaPermessi(contesto : android.content.Context){
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
    private fun apriCamera () {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR code")
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        scanL.launch(options)
    }
    private val scanL = registerForActivityResult(ScanContract()){
        result : ScanIntentResult ->
        run {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            else {

            }
        }
    }

}