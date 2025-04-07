package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomUnityPlayerActivity : AppCompatActivity() {

    private val TAG = "CustomUnityPlayer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

// Nel metodo onCreate
        try {
            val unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayerGameActivity")
            val unityIntent = Intent(this, unityPlayerClass)

            // Passa un valore non vuoto per il parametro "unity"
            unityIntent.putExtra("unity", "-force-vulkan")

            // Copia altri extras se necessario
            intent.extras?.let { originalExtras ->
                unityIntent.putExtras(originalExtras)
            }

            startActivity(unityIntent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Errore completo nell'avvio di Unity: ${e.message}", e)
            Toast.makeText(this, "Errore nell'avvio di Unity: ${e.message}", Toast.LENGTH_LONG)
                .show()
            finish()
        }

    }
}
