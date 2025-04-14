package com.example.nativesparksapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class GameLaunchActivity : AppCompatActivity() {

    private val TAG = "GameLaunchActivity"

    // Preferenze per il form e la Academy Mode
    private val PREFS_NAME = "SparksGamePrefs"
    private val FORM_KEY = "form_already_filled"
    private val KEY_ACADEMY_MODE = "is_academy_mode"

    // View “classiche”
    private lateinit var imageHexPlay: ImageView
    private lateinit var textLinkForm: TextView

    // Container con le 3 card per la modalità Academy
    private lateinit var academyCardsContainer: LinearLayout

    // Le 3 card dell’Academy
    private lateinit var cardQuiz1: CardView
    private lateinit var cardQuiz2: CardView
    private lateinit var cardCourse1: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Bottom Nav
        BottomNavigationHelper.setupBottomNavigation(this)

        // Riferimenti alle View classiche
        imageHexPlay = findViewById(R.id.imageHexPlay)
        textLinkForm = findViewById(R.id.textLinkForm)

        // Container con card Academy
        academyCardsContainer = findViewById(R.id.academyCardsContainer)

        // Le card specifiche
        cardQuiz1 = findViewById(R.id.cardQuiz1)
        cardQuiz2 = findViewById(R.id.cardQuiz2)
        cardCourse1 = findViewById(R.id.cardCourse1)

        // Legge la preferenza: l’utente è in modalità Academy?
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAcademyMode = prefs.getBoolean(KEY_ACADEMY_MODE, false)
        Log.d(TAG, "isAcademyMode = $isAcademyMode")

        if (isAcademyMode) {
            // Se Academy è attivo: Mostra le 3 card, nascondi il “Play Unity” e form
            imageHexPlay.visibility = View.GONE
            textLinkForm.visibility = View.GONE
            academyCardsContainer.visibility = View.VISIBLE

            setupAcademyCards() // Gestione click card Quiz/Corso
        } else {
            // Se Academy è disattivo: Mostra “Play Unity” e form, nascondi le card
            imageHexPlay.visibility = View.VISIBLE
            textLinkForm.visibility = View.VISIBLE
            academyCardsContainer.visibility = View.GONE

            // Gestione del click per avviare Unity
            imageHexPlay.setOnClickListener {
                Log.d(TAG, "Pulsante Play cliccato")
                launchUnityGame()
            }

            // Click sul testo link Form
            textLinkForm.setOnClickListener {
                handleFormClick()
            }
        }
    }

    /**
     * Gestisce il click sulle card dell’Academy
     */
    private fun setupAcademyCards() {
        cardQuiz1.setOnClickListener {
            Toast.makeText(this, "Quiz 1 avviato (TODO)", Toast.LENGTH_SHORT).show()
            // Qui potresti avviare un’Activity di Quiz1, ad esempio
        }
        cardQuiz2.setOnClickListener {
            Toast.makeText(this, "Quiz 2 avviato (TODO)", Toast.LENGTH_SHORT).show()
            // Idem come sopra, per un altro Quiz
        }
        cardCourse1.setOnClickListener {
            Toast.makeText(this, "Corso Avanzato avviato (TODO)", Toast.LENGTH_SHORT).show()
            // Esempio di avvio di un Activity con corsi
        }
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

        Log.d(TAG, "Verifica form per utente: $userId, chiave: $userFormKey, già compilato: $alreadyFilled")

        if (alreadyFilled) {
            Toast.makeText(
                this,
                "Hai già compilato il form!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val formUrl = "https://forms.gle/qy5DZt5RMhE37uE36"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
            try {
                startActivity(intent)

                // Mostra un dialog per confermare la compilazione del form dopo 3 secondi
                Handler().postDelayed({
                    showFormCompletionConfirmation(userFormKey)
                }, 3000)

            } catch (e: android.content.ActivityNotFoundException) {
                Log.e(TAG, "Nessun browser disponibile o errore di apertura form", e)
                Toast.makeText(
                    this,
                    "Impossibile aprire il form, nessun browser disponibile.",
                    Toast.LENGTH_SHORT
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
                Toast.makeText(
                    this,
                    "Grazie per aver compilato il form!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("No") { _, _ ->
                Log.d(TAG, "Utente ha indicato di non aver compilato il form")
                Toast.makeText(
                    this,
                    "Puoi compilare il form in qualsiasi momento.",
                    Toast.LENGTH_SHORT
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
