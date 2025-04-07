package com.example.nativesparksapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Toast

class GameLaunchActivity : AppCompatActivity() {

    private val TAG = "GameLaunchActivity"
    private val PREFS_NAME = "SparksGamePrefs"

    // Chiave in SharedPreferences per vedere se il form è già compilato
    private val FORM_KEY = "form_already_filled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val imageHexPlay: ImageView = findViewById(R.id.imageHexPlay)
        val textLinkForm: TextView = findViewById(R.id.textLinkForm)

        textLinkForm.setOnClickListener {
            handleFormClick()
        }

        // Al click su "Play" avvia direttamente il gioco Unity
        imageHexPlay.setOnClickListener {
            Log.d(TAG, "Pulsante Play cliccato")
            launchUnityGame()
        }

        BottomNavigationHelper.setupBottomNavigation(this)
    }

    private fun handleFormClick() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Ottieni l'ID dell'utente corrente da Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "guest"

        // Usa una chiave specifica per utente
        val userFormKey = "${FORM_KEY}_$userId"

        // Verifica se l'utente ha già compilato il form
        val alreadyFilled = prefs.getBoolean(userFormKey, false)

        // Log per debug
        Log.d(
            TAG,
            "Verifica form per utente: $userId, chiave: $userFormKey, già compilato: $alreadyFilled"
        )

        if (alreadyFilled) {
            android.widget.Toast.makeText(
                this,
                "Hai già compilato il form!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            val formUrl = "https://forms.gle/qy5DZt5RMhE37uE36"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(formUrl))
            try {
                startActivity(intent)

                // Mostra un dialog per confermare la compilazione del form
                android.os.Handler().postDelayed({
                    showFormCompletionConfirmation(userFormKey)
                }, 3000) // Attendi 3 secondi prima di mostrare il dialog

            } catch (e: android.content.ActivityNotFoundException) {
                Log.e(TAG, "Nessun browser disponibile o errore di apertura form", e)
                android.widget.Toast.makeText(
                    this,
                    "Impossibile aprire il form, nessun browser disponibile.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showFormCompletionConfirmation(userFormKey: String) {
        AlertDialog.Builder(this)
            .setTitle("Compilazione form")
            .setMessage("Hai completato la compilazione del form?")
            .setPositiveButton("Sì") { _, _ ->
                // Salva lo stato per questo specifico utente
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(userFormKey, true).apply()
                Log.d(TAG, "Form segnato come compilato per chiave: $userFormKey")
                android.widget.Toast.makeText(
                    this,
                    "Grazie per aver compilato il form!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("No") { _, _ ->
                Log.d(TAG, "Utente ha indicato di non aver compilato il form")
                android.widget.Toast.makeText(
                    this,
                    "Puoi compilare il form in qualsiasi momento.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun launchUnityGame() {
        try {
            // Invece di usare CustomUnityPlayerActivity, prova a usare direttamente UnityPlayerGameActivity
            val intent = Intent()
            intent.setClassName(packageName, "com.unity3d.player.UnityPlayerGameActivity")
            intent.putExtra("unity", "-force-vulkan")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'avvio di Unity: ${e.message}", e)
            Toast.makeText(this, "Errore nell'avvio di Unity: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
}
