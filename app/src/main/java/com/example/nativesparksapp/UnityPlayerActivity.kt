package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class CustomUnityPlayerActivity : AppCompatActivity() {

    private val TAG = "CustomUnityPlayer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Carica dinamicamente la classe UnityPlayerGameActivity
            val unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayerGameActivity")

            // Crea un Intent per lanciarla
            val unityIntent = Intent(this, unityPlayerClass)

            // Se vuoi passare extras, copia quelli dell'Intent corrente
            intent.extras?.let { originalExtras ->
                unityIntent.putExtras(originalExtras)
            }

            // Avvia l'Activity Unity
            startActivity(unityIntent)

            // (Opzionale) Chiudi questa attività per non rimanere sullo stack
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'avvio di Unity: ${e.message}")

            // Mostra un Toast d'errore
            android.widget.Toast.makeText(
                this,
                "Impossibile avviare il gioco Unity: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // Torna indietro all'Activity chiamante
            finish()
        }
    }
}
