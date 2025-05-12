package com.example.nativesparksapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File

class MyScoreActivity : BaseActivity() {

    private lateinit var textScore1: TextView   // ColorSlots high score
    private lateinit var textScore2: TextView   // Combo high score
    private lateinit var cardMode1: MaterialCardView
    private lateinit var cardMode2: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_score)

        BottomNavigationHelper.setupBottomNavigation(this)

        textScore1 = findViewById(R.id.textScore1)
        textScore2 = findViewById(R.id.textScore2)
        cardMode1  = findViewById(R.id.cardMode1)
        cardMode2  = findViewById(R.id.cardMode2)

        // All’avvio mostriamo i record correnti
        displayStoredHighScores()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyScoreActivity", ">> onResume invoked")

        // 1) Calcola i nuovi best scores
        val (newColorHigh, newComboHigh) = calculateBestScores()

        Log.d("MyScoreActivity", "Calculated highs: Color=$newColorHigh, Combo=$newComboHigh")

        // 2) Aggiorna UI
        textScore1.text = newColorHigh.toString()
        textScore2.text = newComboHigh.toString()

        // 3) Sincronizza il best score (il maggiore dei due) su Firestore
        val bestScore = maxOf(newColorHigh, newComboHigh)
        syncHighScoreToFirestore(bestScore)
    }

    private fun calculateBestScores(): Pair<Int,Int> {
        // leggi session prefs di Unity
        val prefsDir = File(filesDir.parent, "shared_prefs")
        val prefsFile = prefsDir.listFiles()
            ?.firstOrNull { it.name.endsWith("v2.playerprefs.xml") }

        // prefs di app
        val appPrefs = getSharedPreferences("MyScorePrefs", Context.MODE_PRIVATE)
        var storedColor = appPrefs.getInt("HighScore_ColorSlots", 0)
        var storedCombo = appPrefs.getInt("HighScore_Combo", 0)

        if (prefsFile != null) {
            val unityPrefs = getSharedPreferences(prefsFile.name.removeSuffix(".xml"), Context.MODE_PRIVATE)
            val sessionColor = unityPrefs.getInt("HighScore_ColorSlots", 0)
            val sessionCombo = unityPrefs.getInt("HighScore_Combo", 0)

            // aggiorna solo se maggiore
            storedColor = maxOf(storedColor, sessionColor)
            storedCombo = maxOf(storedCombo, sessionCombo)

            // non cancelliamo più le chiavi di Unity: manteniamo sempre il record
            // unityPrefs.edit().remove(...).apply()
        }

        // salva i nuovi record in app
        appPrefs.edit()
            .putInt("HighScore_ColorSlots", storedColor)
            .putInt("HighScore_Combo", storedCombo)
            .apply()

        return Pair(storedColor, storedCombo)
    }

    private fun displayStoredHighScores() {
        val appPrefs = getSharedPreferences("MyScorePrefs", Context.MODE_PRIVATE)
        val storedColor = appPrefs.getInt("HighScore_ColorSlots", 0)
        val storedCombo = appPrefs.getInt("HighScore_Combo", 0)
        textScore1.text = storedColor.toString()
        textScore2.text = storedCombo.toString()
    }

    private fun syncHighScoreToFirestore(bestScore: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("MyScoreActivity", "Utente non autenticato, skip Firestore sync")
            return
        }

        val collectionName = "leaderboard"  // ATTENZIONE: minuscolo, esatto nome in console
        val uid = user.uid
        val displayName = user.displayName ?: "Anonimo"
        val data = mapOf(
            "displayName" to displayName,
            "score"       to bestScore,
            "avatarUrl"   to "default_avatar.png",
            "timestamp"   to FieldValue.serverTimestamp()
        )

        Log.d("MyScoreActivity", "Sync su $collectionName/$uid con $data")

        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("MyScoreActivity", "✔ Firestore save successo per $uid")
            }
            .addOnFailureListener { e ->
                Log.e("MyScoreActivity", "❌ Firestore save FALLITO per $uid", e)
            }
    }
}
