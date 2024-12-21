package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
        private lateinit var allenametoLayout: ConstraintLayout
        private lateinit var scanner: FloatingActionButton
        private lateinit var terminaButton: Button
        private lateinit var sessionManager: SessionManager
        private lateinit var menuButton: Button
        private lateinit var cardView: CardView
        private lateinit var skipButton: Button
        private  lateinit var schedaLabel :TextView
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val binding = inflater.inflate(R.layout.fragment_home, container, false)
            menuButton = binding.findViewById(R.id.menuButton)
            menuButton.setOnClickListener { v ->
                showPopupMenu(v)
            }

            allenametoLayout = binding.findViewById(R.id.allenametoLayout)
            scanner = binding.findViewById(R.id.floatingActionButton2)
            terminaButton = binding.findViewById(R.id.terminaButton)
            cardView = binding.findViewById(R.id.cardworkout)
            skipButton = binding.findViewById(R.id.skipButton)
            schedaLabel = binding.findViewById(R.id.schedaLabel)
            skipButton.setOnClickListener {
                sessionManager.skipWorkout()
            }

            val user = FirebaseAuth.getInstance().currentUser
            sessionManager = SessionManager(user?.uid ?: "")

            // Listen for session changes in real-time
            listenForSessionChanges()

            // Start QR scan when the scanner button is clicked
            scanner.setOnClickListener {
                verificaPermessi(requireContext())
            }

            // End session when the termina button is clicked
            terminaButton.setOnClickListener {
                sessionManager.terminateSession()
                allenametoLayout.visibility = View.GONE
                scanner.visibility = View.VISIBLE
            }

            return binding
        }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)

        // Get reference to Firebase "schede" node for the current user
        val databaseReference = FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance("https://gymapp-48c7e-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(it.uid) // Use UID instead of `toString()`
                .child("schede")
        }

        // Fetch data from Firebase
        databaseReference?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val menu = popupMenu.menu
                val schede = mutableListOf<Scheda>()
                snapshot?.children?.forEachIndexed { index, dataSnapshot ->
                    val menuItemTitle = dataSnapshot.child("nome").getValue(String::class.java)
                    schede.add(dataSnapshot.getValue(Scheda::class.java)!!)
                    menu.add(0, index, 0, menuItemTitle)
                }

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    val item = schede[item.itemId]
                    Log.d("DEBUG", "Scheda selezionata: ${item}")
                    sessionManager.setSessionScheda(item)
                    allenametoLayout.visibility = View.VISIBLE
                    menuButton.visibility = View.GONE
                    Toast.makeText(requireContext(), "${item.nome} clicked", Toast.LENGTH_SHORT).show()
                    true


                }

                // Show the PopupMenu after adding items
                popupMenu.show()
            } else {
                // Handle error if Firebase fetch fails
                Toast.makeText(requireContext(), "Failed to load data from Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }
        private fun listenForSessionChanges() {
            sessionManager.listenForSessionChanges {isInSession, snapshot ->
                Log.d("DEBUG", "Session status: $isInSession - Snapshot: $snapshot")
                if (isInSession) {
                    scanner.visibility = View.GONE
                    if (snapshot != null) {
                        if (!snapshot.child("scheda").exists()) {
                            menuButton.visibility = View.VISIBLE
                        }else{
                            menuButton.visibility = View.GONE
                            allenametoLayout.visibility = View.VISIBLE

                            val currentWorkout = sessionManager.getCurrentWorkout()
                            cardView.findViewById<TextView>(R.id.titoloTextView).text = currentWorkout?.titolo
                            cardView.findViewById<TextView>(R.id.descrizioneTextView).text = currentWorkout?.descrizione
                            schedaLabel.text = "SCHEDA: ${sessionManager.currentScheda?.nome}"
                            val imageUrl = currentWorkout?.url
                            if (!imageUrl.isNullOrEmpty()) {
                                val imageView = cardView.findViewById<ImageView>(R.id.imageView)
                                Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.squatbilanciere)
                                    .error(R.drawable.errore_immagine)
                                    .into(imageView)
                            }
                        }
                    }
                } else {
                    allenametoLayout.visibility = View.GONE
                    scanner.visibility = View.VISIBLE
                    menuButton.visibility = View.GONE
                }
            }
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
            sessionManager.terminateSession()
            allenametoLayout.visibility = View.GONE
            scanner.visibility = View.VISIBLE
            sessionManager.terminateSession()
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
