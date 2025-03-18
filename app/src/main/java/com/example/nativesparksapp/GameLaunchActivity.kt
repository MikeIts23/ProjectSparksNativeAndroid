package com.example.nativesparksapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val imageHexPlay = findViewById<ImageView>(R.id.imageHexPlay)

        imageHexPlay.setOnClickListener {
            launchUnityGame()
        }
    }

    private fun launchUnityGame() {
        try {
            val intent = Intent(this, Class.forName("com.unity3d.player.UnityPlayerActivity"))
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            Toast.makeText(this, "Errore nell'avvio del gioco Unity.", Toast.LENGTH_LONG).show()
        }
    }
}
