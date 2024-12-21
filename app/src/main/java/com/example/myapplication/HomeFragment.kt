package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import org.w3c.dom.Text
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var scanner: FloatingActionButton
    private lateinit var allenametoLayout: ConstraintLayout
    private lateinit var terminaButton : Button
    private lateinit var session : SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val user = FirebaseAuth.getInstance().currentUser
        session = user?.uid?.let { SessionManager(it) }!!
        if (session != null) {

            lifecycleScope.launch {
                Log.d("DEBUGSESSION", session.isUserInSession().toString())

                if (session.isUserInSession()){
                    allenametoLayout.visibility = View.VISIBLE
                    scanner.visibility = View.GONE
                }else {
                    allenametoLayout.visibility = View.GONE
                    scanner.visibility = View.VISIBLE
                }
            }
        }

        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allenametoLayout = view.findViewById(R.id.allenametoLayout)
        scanner = view.findViewById(R.id.floatingActionButton2)
        terminaButton = view.findViewById(R.id.terminaButton)
        scanner.setOnClickListener {
            verificaPermessi(requireContext())
        }
        terminaButton.setOnClickListener {
            session.terminateSession()
            allenametoLayout.visibility = View.GONE
            scanner.visibility = View.VISIBLE
        }

        val timerDuration = 60000L // 1 minute in milliseconds
        val timerTextView: TextView = view.findViewById(R.id.timerTextView)
        val timerButton: Button = view.findViewById(R.id.timerButton)
        var timer: CountDownTimer? = null
        var isTimerRunning = false
        var timeRemaining = timerDuration // Tracks the remaining time
        timerTextView.text = "01:00"
        timerButton.text = "Start"
        timerButton.setOnClickListener {

            if (isTimerRunning) {
                // Pause the timer
                isTimerRunning = false
                timerButton.text = "Resume"
                timer?.cancel() // Stop the current timer but keep the remaining time
            } else {
                // Start or Resume the timer
                isTimerRunning = true
                timerButton.text = "Pause"

                timer = object : CountDownTimer(timeRemaining, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        timeRemaining = millisUntilFinished // Update the remaining time
                        val seconds = millisUntilFinished / 1000
                        timerTextView.text = String.format("%02d:%02d", seconds / 60, seconds % 60)
                    }

                    override fun onFinish() {
                        timerTextView.text = "00:00"
                        isTimerRunning = false
                        timerButton.text = "Start"
                        timeRemaining = timerDuration // Reset for the next start
                    }
                }.start()
            }
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
                Toast.makeText(requireContext(), "Impossibile recuperare il token", Toast.LENGTH_SHORT).show()
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.message?.let { Log.d("qrscanner", it) }
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        val responseData = response.body?.string()
                        if (!response.isSuccessful) {
                            requireActivity().runOnUiThread {
                                val errorMessage = when (response.code) {
                                    403 -> "NON HAI INGRESSI DISPONIBILI"
                                    404 -> "Errore API: UTENTE NON REGISTRATO"
                                    else -> "Errore API: ${response.code}"
                                }
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                                Log.e("qrscanner", "API Error: $responseData")
                            }
                        } else {
                            val apiResponse = Gson().fromJson(responseData, ApiResponse::class.java)
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Risposta API: ${apiResponse.message}", Toast.LENGTH_LONG).show()
                                Log.d("qrscanner", "$responseData")
                                scanner.visibility = View.GONE
                                allenametoLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }

            })
        }
    }

    private val scanL = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Scansione annullata", Toast.LENGTH_SHORT).show()
        } else {
            val scannedUrl = result.contents

            // Effettua la chiamata all'API
            inviaRichiestaAPI(scannedUrl)
        }
    }
    private val richiestaPermesso = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted ->
        if (isGranted){
            apriCamera()
        }
        else {
            Toast.makeText(requireContext(),"Ho bisogno dei permessi per accedere alla fotocamera", Toast.LENGTH_SHORT).show()
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
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
