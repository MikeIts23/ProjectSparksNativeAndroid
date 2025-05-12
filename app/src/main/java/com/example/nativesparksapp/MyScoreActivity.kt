package com.example.nativesparksapp

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import java.io.File

class MyScoreActivity : BaseActivity() {

    private lateinit var textScore1: TextView  // ColorSlots high score
    private lateinit var textScore2: TextView  // Combo high score
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
        // 1) Leggi i punteggi appena scritti da Unity
        val prefsDir = File(filesDir.parent, "shared_prefs")
        val prefsFile = prefsDir.listFiles()
            ?.firstOrNull { it.name.endsWith("v2.playerprefs.xml") }
            ?: return

        val prefsName = prefsFile.name.removeSuffix(".xml")
        val unityPrefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        val sessionColor = unityPrefs.getInt("HighScore_ColorSlots", 0)
        val sessionCombo = unityPrefs.getInt("HighScore_Combo", 0)

        // Pulisci le chiavi Unity per non rileggerle alla prossima onResume
        unityPrefs.edit()
            .remove("HighScore_ColorSlots")
            .remove("HighScore_Combo")
            .apply()

        // 2) Carica i record prima salvati in app
        val appPrefs = getSharedPreferences("MyScorePrefs", Context.MODE_PRIVATE)
        val storedColor = appPrefs.getInt("HighScore_ColorSlots", 0)
        val storedCombo = appPrefs.getInt("HighScore_Combo", 0)

        // 3) Applica la stessa logica di UpdateHighScore di Unity:
        //    solo se il nuovo è maggiore, sostituisci il record
        val newColorHigh = if (sessionColor > storedColor) sessionColor else storedColor
        val newComboHigh = if (sessionCombo > storedCombo) sessionCombo else storedCombo

        // 4) Salva i nuovi record nell’app
        appPrefs.edit()
            .putInt("HighScore_ColorSlots", newColorHigh)
            .putInt("HighScore_Combo", newComboHigh)
            .apply()

        // 5) Mostra a schermo
        textScore1.text = newColorHigh.toString()
        textScore2.text = newComboHigh.toString()
    }

    private fun displayStoredHighScores() {
        val appPrefs = getSharedPreferences("MyScorePrefs", Context.MODE_PRIVATE)
        val storedColor = appPrefs.getInt("HighScore_ColorSlots", 0)
        val storedCombo = appPrefs.getInt("HighScore_Combo", 0)
        textScore1.text = storedColor.toString()
        textScore2.text = storedCombo.toString()
    }
}
